package com.ian.web.employee.leave;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ian.web.employee.Employee;
import com.ian.web.employee.leave.LeaveApplication.LeaveStatus;
import com.ian.web.employee.leave.LeaveLedger.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Core business rules for government leave management.
 *
 * CSC / Legal basis:
 *   VL  – Vacation Leave         CSC MC No. 41 s.1998 — 1.25 days/month, carry-over max 30
 *   SL  – Sick Leave             CSC MC No. 41 s.1998 — 1.25 days/month, carry-over max 30
 *   FL  – Forced/Mandatory Leave 5 days/year deducted from VL, all permanent
 *   SPL – Special Privilege Leave 3 days/year, non-accrual
 *   ML  – Maternity Leave        RA 11210 — 105 days (solo parent 120), female only
 *   PL  – Paternity Leave        RA 8187  — 7 days, male only
 *   SLB – Special Leave Benefit  RA 9710  — Gynecological, female only
 *   LWOP– Leave Without Pay      always available, affects service credit
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveService {

    private final LeaveApplicationRepository   applicationRepo;
    private final LeaveBalanceRepository       balanceRepo;
    private final LeaveLedgerRepository        ledgerRepo;
    private final LeaveTypeRepository          leaveTypeRepo;
    private final LeaveStatusHistoryRepository historyRepo;
    private final PublicHolidayRepository      holidayRepo;
    private final LeaveEmailService            emailService;

    // -----------------------------------------------------------------------
    // APPLICATION LIFECYCLE
    // -----------------------------------------------------------------------

    /**
     * Employee submits a new leave application (status = PENDING).
     * Validates date range, gender eligibility, and date overlap.
     * Does NOT deduct balance — that happens at APPROVAL time.
     */
    @Transactional
    public LeaveApplication applyLeave(LeaveApplication application) {
        validateDateRange(application.getDateFrom(), application.getDateTo());
        validateGenderEligibility(application.getEmployee(), application.getLeaveType());
        validateNoOverlap(application.getEmployee().getId(),
                application.getDateFrom(), application.getDateTo(), null);

        double workingDays = countWorkingDays(application.getDateFrom(), application.getDateTo());
        application.setNumberOfDays(workingDays);

        // Extended leave policy flag (CSC: >5 working days)
        Integer extThreshold = application.getLeaveType().getExtendedLeaveDays();
        int threshold = (extThreshold != null) ? extThreshold : 0;
        application.setRequiresHigherApproval(threshold > 0 && workingDays > threshold);

        application.setStatus(LeaveStatus.PENDING);
        application.setAppliedDateTime(LocalDateTime.now());
        application.setLedgerPosted(false);

        LeaveApplication saved = applicationRepo.save(application);
        recordHistory(saved, null, LeaveStatus.PENDING,
                application.getEmployee().getId(),
                application.getEmployee().getDisplayName(),
                "Employee", "Application submitted.");
        emailService.notifyApplied(saved);
        return saved;
    }

    /**
     * HR Admin endorses a PENDING application (PENDING → ENDORSED).
     * Records supervisor recommendation and HRMO certification of available credits.
     */
    @Transactional
    public LeaveApplication endorseLeave(Long applicationId,
                                          Long actorId, String actorName,
                                          String recommendation, String remarks,
                                          Double certWithPay, Double certWithoutPay) {
        LeaveApplication app = findOrThrow(applicationId);
        if (app.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only PENDING applications can be endorsed.");
        }

        app.setSupervisorId(actorId);
        app.setSupervisorName(actorName);
        app.setSupervisorActionDate(LocalDate.now());
        app.setSupervisorRecommendation(recommendation != null ? recommendation : "APPROVED_WITH_PAY");
        app.setSupervisorRemarks(remarks);

        // HRMO certification of leave credits
        app.setHrmoId(actorId);
        app.setHrmoName(actorName);
        app.setHrmoCertDate(LocalDate.now());
        app.setCertDaysWithPay(certWithPay != null ? certWithPay : app.getNumberOfDays());
        app.setCertDaysWithoutPay(certWithoutPay != null ? certWithoutPay : 0.0);

        LeaveStatus prev = app.getStatus();
        app.setStatus(LeaveStatus.ENDORSED);
        LeaveApplication saved = applicationRepo.save(app);
        recordHistory(saved, prev, LeaveStatus.ENDORSED, actorId, actorName, "HR Admin",
                "Endorsed. Recommendation: " + app.getSupervisorRecommendation()
                        + (remarks != null && !remarks.isBlank() ? " — " + remarks : ""));
        emailService.notifyEndorsed(saved);
        return saved;
    }

    /**
     * HR Admin returns an application for correction (PENDING|ENDORSED → RETURNED).
     * Employee must re-submit after correcting.
     */
    @Transactional
    public LeaveApplication returnForCorrection(Long applicationId,
                                                 Long actorId, String actorName,
                                                 String remarks) {
        LeaveApplication app = findOrThrow(applicationId);
        if (app.getStatus() != LeaveStatus.PENDING && app.getStatus() != LeaveStatus.ENDORSED) {
            throw new IllegalStateException(
                    "Only PENDING or ENDORSED applications can be returned for correction.");
        }
        LeaveStatus prev = app.getStatus();
        app.setStatus(LeaveStatus.RETURNED);
        app.setRemarks(remarks);
        LeaveApplication saved = applicationRepo.save(app);
        recordHistory(saved, prev, LeaveStatus.RETURNED, actorId, actorName, "HR Admin",
                "Returned for correction: " + (remarks != null ? remarks : ""));
        emailService.notifyReturned(saved);
        return saved;
    }

    /**
     * Employee re-submits a RETURNED application (RETURNED → PENDING).
     */
    @Transactional
    public LeaveApplication resubmitLeave(Long applicationId, Long employeeId,
                                           String reason, String leaveDetails,
                                           String leaveSubType) {
        LeaveApplication app = findOrThrow(applicationId);
        if (app.getStatus() != LeaveStatus.RETURNED) {
            throw new IllegalStateException("Only RETURNED applications can be re-submitted.");
        }
        if (employeeId == null || app.getEmployee().getId() != employeeId.longValue()) {
            throw new SecurityException("You may only re-submit your own applications.");
        }

        if (reason != null && !reason.isBlank())       app.setReason(reason);
        if (leaveDetails != null)                       app.setLeaveDetails(leaveDetails);
        if (leaveSubType != null)                       app.setLeaveSubType(leaveSubType);

        // Re-compute working days in case dates are unchanged but holidays changed
        app.setNumberOfDays(countWorkingDays(app.getDateFrom(), app.getDateTo()));

        // Re-evaluate extended leave flag (employee may have narrowed the date range)
        Integer resubThreshold = app.getLeaveType().getExtendedLeaveDays();
        int threshold = (resubThreshold != null) ? resubThreshold : 0;
        app.setRequiresHigherApproval(
                threshold > 0 && app.getNumberOfDays() > threshold);

        // Clear previous endorsement data so HR must re-process
        app.setSupervisorId(null);      app.setSupervisorName(null);
        app.setSupervisorActionDate(null); app.setSupervisorRecommendation(null);
        app.setSupervisorRemarks(null);
        app.setHrmoId(null);            app.setHrmoName(null);
        app.setHrmoCertDate(null);
        app.setCertDaysWithPay(null);   app.setCertDaysWithoutPay(null);
        app.setRemarks(null);

        app.setStatus(LeaveStatus.PENDING);
        LeaveApplication saved = applicationRepo.save(app);
        recordHistory(saved, LeaveStatus.RETURNED, LeaveStatus.PENDING,
                employeeId, app.getEmployee().getDisplayName(),
                "Employee", "Re-submitted after correction.");
        return saved;
    }

    /**
     * Admin approves a PENDING or ENDORSED application.
     * Deducts from leave balance and posts a DEBIT ledger entry.
     * If balance is insufficient and leave type is not LWOP, marks as LWOP (withPay=false).
     */
    @Transactional
    public LeaveApplication approveLeave(Long applicationId,
                                          Long approverEmployeeId, String approverName,
                                          String remarks) {
        LeaveApplication app = findOrThrow(applicationId);
        if (app.getStatus() != LeaveStatus.PENDING && app.getStatus() != LeaveStatus.ENDORSED) {
            throw new IllegalStateException(
                    "Only PENDING or ENDORSED applications can be approved.");
        }

        // Extended leave policy: endorsement is mandatory before approval
        if (app.isRequiresHigherApproval()
                && (app.getLeaveType().getRequiresEndorsementForExtended() == Boolean.TRUE)
                && app.getStatus() == LeaveStatus.PENDING) {
            Integer thr = app.getLeaveType().getExtendedLeaveDays();
            int threshold = (thr != null) ? thr : 0;
            throw new IllegalStateException(
                "Leave application #" + applicationId + " exceeds " + threshold
                + " working days and must be endorsed before it can be approved.");
        }

        int year = app.getDateFrom().getYear();
        LeaveBalance balance = getOrCreateBalance(app.getEmployee(), app.getLeaveType(), year);

        boolean withPay = true;
        boolean debitPosted = false;

        if (app.getLeaveType().isLwopType()) {
            // LWOP type — no balance deduction, always without pay
            withPay = false;
        } else if (balance.getBalance() < app.getNumberOfDays()) {
            // Insufficient balance — treat as LWOP
            withPay = false;
        } else {
            balance.setTotalUsed(balance.getTotalUsed() + app.getNumberOfDays());
            balance.setBalance(balance.getTotalEarned() - balance.getTotalUsed());
            balanceRepo.save(balance);

            LeaveLedger debit = new LeaveLedger();
            debit.setEmployee(app.getEmployee());
            debit.setLeaveType(app.getLeaveType());
            debit.setTransactionDate(LocalDate.now());
            debit.setTransactionType(TransactionType.DEBIT);
            debit.setDays(-app.getNumberOfDays());
            debit.setRunningBalance(balance.getBalance());
            debit.setReference("APP-" + applicationId);
            debit.setRemarks("Approved leave application #" + applicationId);
            ledgerRepo.save(debit);
            debitPosted = true;
        }

        // Set final approved days breakdown
        app.setFinalDaysWithPay(withPay ? app.getNumberOfDays() : 0.0);
        app.setFinalDaysWithoutPay(withPay ? 0.0 : app.getNumberOfDays());

        LeaveStatus prev = app.getStatus();
        app.setStatus(LeaveStatus.APPROVED);
        app.setWithPay(withPay);
        app.setApprovedById(approverEmployeeId);
        app.setApprovedByName(approverName);
        app.setApprovedDate(LocalDate.now());
        app.setRemarks(remarks);
        // ledgerPosted is true only when a DEBIT entry was actually written to the ledger
        app.setLedgerPosted(debitPosted);
        LeaveApplication saved = applicationRepo.save(app);
        recordHistory(saved, prev, LeaveStatus.APPROVED, approverEmployeeId, approverName,
                "HR Admin",
                "Approved" + (withPay ? " with pay." : " as LWOP (insufficient balance).")
                        + (remarks != null && !remarks.isBlank() ? " Remarks: " + remarks : ""));
        emailService.notifyApproved(saved);
        return saved;
    }

    /**
     * Admin disapproves a PENDING or ENDORSED application (no balance impact).
     */
    @Transactional
    public LeaveApplication disapproveLeave(Long applicationId,
                                             Long approverEmployeeId, String approverName,
                                             String remarks) {
        LeaveApplication app = findOrThrow(applicationId);
        if (app.getStatus() != LeaveStatus.PENDING && app.getStatus() != LeaveStatus.ENDORSED) {
            throw new IllegalStateException(
                    "Only PENDING or ENDORSED applications can be disapproved.");
        }
        LeaveStatus prev = app.getStatus();
        app.setStatus(LeaveStatus.DISAPPROVED);
        app.setApprovedById(approverEmployeeId);
        app.setApprovedByName(approverName);
        app.setApprovedDate(LocalDate.now());
        app.setRemarks(remarks);
        LeaveApplication saved = applicationRepo.save(app);
        recordHistory(saved, prev, LeaveStatus.DISAPPROVED, approverEmployeeId, approverName,
                "HR Admin",
                "Disapproved."
                        + (remarks != null && !remarks.isBlank() ? " Reason: " + remarks : ""));
        emailService.notifyDisapproved(saved);
        return saved;
    }

    /**
     * Employee or admin cancels an application.
     * If already approved and ledger was posted, reverses the deduction.
     *
     * @param actorId   ID of the person cancelling (null = system)
     * @param actorName Display name of the person cancelling
     * @param reason    Optional reason for cancellation
     */
    @Transactional
    public LeaveApplication cancelLeave(Long applicationId,
                                         Long actorId, String actorName, String reason) {
        LeaveApplication app = findOrThrow(applicationId);
        if (app.getStatus() == LeaveStatus.CANCELLED) {
            throw new IllegalStateException("Application is already cancelled.");
        }
        if (app.getStatus() == LeaveStatus.DISAPPROVED) {
            throw new IllegalStateException("A disapproved application cannot be cancelled.");
        }

        // Reverse ledger debit if leave was approved with pay
        if (app.getStatus() == LeaveStatus.APPROVED && app.isLedgerPosted() && app.isWithPay()) {
            int year = app.getDateFrom().getYear();
            LeaveBalance balance = getOrCreateBalance(app.getEmployee(), app.getLeaveType(), year);
            balance.setTotalUsed(Math.max(0, balance.getTotalUsed() - app.getNumberOfDays()));
            balance.setBalance(balance.getTotalEarned() - balance.getTotalUsed());
            balanceRepo.save(balance);

            LeaveLedger void_ = new LeaveLedger();
            void_.setEmployee(app.getEmployee());
            void_.setLeaveType(app.getLeaveType());
            void_.setTransactionDate(LocalDate.now());
            void_.setTransactionType(TransactionType.VOID);
            void_.setDays(app.getNumberOfDays());
            void_.setRunningBalance(balance.getBalance());
            void_.setReference("VOID-APP-" + applicationId);
            void_.setRemarks("Cancellation reversal for leave application #" + applicationId);
            ledgerRepo.save(void_);
        }

        // Record cancellation metadata
        app.setCancelledById(actorId);
        app.setCancelledByName(actorName);
        app.setCancelledDate(LocalDate.now());
        app.setCancelReason(reason);

        LeaveStatus prev = app.getStatus();
        app.setStatus(LeaveStatus.CANCELLED);
        LeaveApplication saved = applicationRepo.save(app);
        String historyRemarks = "Application cancelled"
                + (reason != null && !reason.isBlank() ? ". Reason: " + reason : ".");
        recordHistory(saved, prev, LeaveStatus.CANCELLED, actorId, actorName,
                actorId != null ? "HR Admin" : "System", historyRemarks);
        emailService.notifyCancelled(saved);
        return saved;
    }

    /** Convenience overload for backward compatibility (system-initiated cancel). */
    @Transactional
    public LeaveApplication cancelLeave(Long applicationId) {
        return cancelLeave(applicationId, null, "System", null);
    }

    // -----------------------------------------------------------------------
    // BALANCE QUERIES
    // -----------------------------------------------------------------------

    public List<LeaveBalance> getBalancesForEmployee(Long employeeId, int year) {
        return balanceRepo.findByEmployeeIdAndYearFetched(employeeId, year);
    }

    public List<LeaveLedger> getLedgerForEmployee(Long employeeId) {
        return ledgerRepo.findByEmployeeIdOrderByTransactionDateDesc(employeeId);
    }

    /**
     * Same as getLedgerForEmployee but with leaveType JOIN-FETCHed.
     * Use this for templates that display ledger.leaveType.leaveName to avoid
     * LazyInitializationException when spring.jpa.open-in-view=false.
     */
    public List<LeaveLedger> getLedgerForEmployeeFetched(Long employeeId) {
        return ledgerRepo.findByEmployeeIdFetched(employeeId);
    }

    public List<LeaveLedger> getLedgerForEmployeeAndType(Long employeeId, Long leaveTypeId) {
        return ledgerRepo.findByEmployeeIdAndLeaveTypeIdOrderByTransactionDateDesc(employeeId, leaveTypeId);
    }

    public List<LeaveStatusHistory> getStatusHistory(Long applicationId) {
        return historyRepo.findByApplicationIdOrderByChangedAtAsc(applicationId);
    }

    // -----------------------------------------------------------------------
    // ACCRUAL (called by scheduler monthly)
    // -----------------------------------------------------------------------

    /**
     * Credits monthly accrual for leave types with accrualPerMonth > 0
     * (VL and SL = 1.25 days each). Call on the last working day of the month.
     */
    @Transactional
    public void accrueMonthlyLeave(Employee employee, int year, int month) {
        List<LeaveType> types = leaveTypeRepo.findByActiveTrueOrderBySortOrderAscLeaveNameAsc();
        for (LeaveType type : types) {
            if (type.getAccrualPerMonth() <= 0) continue;
            if (!isEligible(employee, type)) continue;

            String ref = "ACCRUAL-" + year + "-" + String.format("%02d", month);
            // Idempotency: skip if this month's accrual was already posted
            boolean alreadyPosted = ledgerRepo
                    .findByEmployeeIdAndReference(employee.getId(), ref)
                    .stream()
                    .anyMatch(l -> l.getLeaveType() != null
                            && l.getLeaveType().getId().equals(type.getId()));
            if (alreadyPosted) continue;

            LeaveBalance balance = getOrCreateBalance(employee, type, year);
            double credit = type.getAccrualPerMonth();
            balance.setTotalEarned(balance.getTotalEarned() + credit);
            balance.setBalance(balance.getTotalEarned() - balance.getTotalUsed());
            balanceRepo.save(balance);

            LeaveLedger entry = new LeaveLedger();
            entry.setEmployee(employee);
            entry.setLeaveType(type);
            entry.setTransactionDate(LocalDate.of(year, month, 1));
            entry.setTransactionType(TransactionType.CREDIT);
            entry.setDays(credit);
            entry.setRunningBalance(balance.getBalance());
            entry.setReference(ref);
            entry.setRemarks("Monthly accrual for " + type.getLeaveName());
            ledgerRepo.save(entry);
        }
    }

    /**
     * Carry over unused balance from previousYear to currentYear,
     * capped by leaveType.carryOverMax.
     */
    @Transactional
    public void carryOver(Employee employee, int previousYear, int currentYear) {
        List<LeaveBalance> prev = balanceRepo.findByEmployeeIdAndBalanceYear(employee.getId(), previousYear);
        for (LeaveBalance old : prev) {
            if (old.getLeaveType().getCarryOverMax() <= 0) continue;
            double carryAmount = Math.min(old.getBalance(), old.getLeaveType().getCarryOverMax());
            if (carryAmount <= 0) continue;

            LeaveBalance newBal = getOrCreateBalance(employee, old.getLeaveType(), currentYear);
            newBal.setTotalEarned(newBal.getTotalEarned() + carryAmount);
            newBal.setBalance(newBal.getTotalEarned() - newBal.getTotalUsed());
            balanceRepo.save(newBal);

            LeaveLedger entry = new LeaveLedger();
            entry.setEmployee(employee);
            entry.setLeaveType(old.getLeaveType());
            entry.setTransactionDate(LocalDate.of(currentYear, 1, 1));
            entry.setTransactionType(TransactionType.CREDIT);
            entry.setDays(carryAmount);
            entry.setRunningBalance(newBal.getBalance());
            entry.setReference("CARRYOVER-" + previousYear + "-TO-" + currentYear);
            entry.setRemarks("Carry-over from " + previousYear);
            ledgerRepo.save(entry);
        }
    }

    /**
     * Admin manual credit/debit adjustment with full ledger trail.
     */
    @Transactional
    public void adjustBalance(Employee employee, LeaveType leaveType, int year,
                              double days, String reference, String remarks) {
        LeaveBalance balance = getOrCreateBalance(employee, leaveType, year);
        if (days > 0) {
            balance.setTotalEarned(balance.getTotalEarned() + days);
        } else {
            balance.setTotalUsed(balance.getTotalUsed() + Math.abs(days));
        }
        balance.setBalance(balance.getTotalEarned() - balance.getTotalUsed());
        balanceRepo.save(balance);

        LeaveLedger entry = new LeaveLedger();
        entry.setEmployee(employee);
        entry.setLeaveType(leaveType);
        entry.setTransactionDate(LocalDate.now());
        entry.setTransactionType(TransactionType.ADJUSTMENT);
        entry.setDays(days);
        entry.setRunningBalance(balance.getBalance());
        entry.setReference(reference != null ? reference : "ADJ-" + System.currentTimeMillis());
        entry.setRemarks(remarks);
        ledgerRepo.save(entry);
    }

    // -----------------------------------------------------------------------
    // WORKING DAY CALCULATION — holiday-aware
    // -----------------------------------------------------------------------

    /**
     * Counts working days (Mon–Fri) between two dates, inclusive,
     * excluding public holidays marked as excludeFromLeave=true.
     * Philippine government standard: 5-day workweek.
     */
    public double countWorkingDays(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) return 0;

        Set<LocalDate> holidayDates = holidayRepo
                .findExcludedHolidaysBetween(from, to)
                .stream()
                .map(PublicHoliday::getHolidayDate)
                .collect(Collectors.toSet());

        long count = 0;
        LocalDate date = from;
        while (!date.isAfter(to)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY
                    && !holidayDates.contains(date)) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }

    // -----------------------------------------------------------------------
    // VALIDATION HELPERS
    // -----------------------------------------------------------------------

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Leave start and end dates are required.");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("End date must be on or after start date.");
        }
        if (from.isBefore(LocalDate.now().minusMonths(3))) {
            throw new IllegalArgumentException(
                    "Leave applications cannot be filed more than 3 months in the past. " +
                    "Contact HR for retroactive balance adjustments.");
        }
    }

    /**
     * Gender-based eligibility check per CSC / Republic Acts.
     *   ML  (Maternity Leave)        RA 11210 — female employees only
     *   PL  (Paternity Leave)        RA 8187  — male employees only
     *   SLB (Special Leave Benefit)  RA 9710  — female employees only
     */
    private void validateGenderEligibility(Employee employee, LeaveType leaveType) {
        String code   = leaveType.getLeaveCode();
        String gender = employee.getGender();
        if (gender == null) return;

        boolean isFemale = gender.equalsIgnoreCase("FEMALE") || gender.equalsIgnoreCase("F");
        boolean isMale   = gender.equalsIgnoreCase("MALE")   || gender.equalsIgnoreCase("M");

        if (("ML".equals(code) || "SLB".equals(code)) && !isFemale) {
            throw new IllegalArgumentException(
                    leaveType.getLeaveName() + " is only available to female employees.");
        }
        if ("PL".equals(code) && !isMale) {
            throw new IllegalArgumentException(
                    leaveType.getLeaveName() + " is only available to male employees.");
        }
    }

    /**
     * Overlap check — prevents filing duplicate/overlapping leaves.
     * Pass excludeId=null for new applications.
     */
    private void validateNoOverlap(Long employeeId, LocalDate from, LocalDate to, Long excludeId) {
        java.util.List<LeaveApplication.LeaveStatus> terminal = java.util.Arrays.asList(
                LeaveApplication.LeaveStatus.CANCELLED,
                LeaveApplication.LeaveStatus.DISAPPROVED,
                LeaveApplication.LeaveStatus.RETURNED);
        List<LeaveApplication> overlapping = excludeId == null
                ? applicationRepo.findOverlapping(employeeId, from, to, terminal)
                : applicationRepo.findOverlappingExcluding(employeeId, excludeId, from, to, terminal);

        if (!overlapping.isEmpty()) {
            LeaveApplication conflict = overlapping.get(0);
            throw new IllegalArgumentException(
                    "Overlapping leave application exists: #" + conflict.getId()
                    + " (" + conflict.getLeaveType().getLeaveName() + ", "
                    + conflict.getDateFrom() + " to " + conflict.getDateTo()
                    + ", status: " + conflict.getStatus() + "). "
                    + "Cancel or wait for the existing application to be processed first.");
        }
    }

    // -----------------------------------------------------------------------
    // PRIVATE HELPERS
    // -----------------------------------------------------------------------

    private LeaveApplication findOrThrow(Long id) {
        return applicationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Leave application not found: " + id));
    }

    public LeaveBalance getOrCreateBalance(Employee employee, LeaveType leaveType, int year) {
        Optional<LeaveBalance> opt = balanceRepo
                .findByEmployeeIdAndLeaveTypeIdAndBalanceYear(
                        employee.getId(), leaveType.getId(), year);
        if (opt.isPresent()) return opt.get();

        LeaveBalance newBal = new LeaveBalance();
        newBal.setEmployee(employee);
        newBal.setLeaveType(leaveType);
        newBal.setBalanceYear(year);
        newBal.setTotalEarned(0);
        newBal.setTotalUsed(0);
        newBal.setBalance(0);
        return balanceRepo.save(newBal);
    }

    private boolean isEligible(Employee employee, LeaveType leaveType) {
        String eligible = leaveType.getEligibleEmploymentTypes();
        if (eligible == null || eligible.trim().isEmpty()) return true;
        if (employee.getEmployeeStatus() == null) return false;
        String empType = employee.getEmployeeStatus().getEmploymentType();
        if (empType == null) return false;
        for (String t : eligible.split(",")) {
            if (t.trim().equalsIgnoreCase(empType.trim())) return true;
        }
        return false;
    }

    private void recordHistory(LeaveApplication app,
                                LeaveStatus fromStatus, LeaveStatus toStatus,
                                Long actorId, String actorName,
                                String actorRole, String remarks) {
        try {
            LeaveStatusHistory h = new LeaveStatusHistory();
            h.setApplication(app);
            h.setFromStatus(fromStatus);
            h.setToStatus(toStatus);
            h.setActorId(actorId);
            h.setActorName(actorName);
            h.setActorRole(actorRole);
            h.setRemarks(remarks);
            h.setChangedAt(LocalDateTime.now());
            historyRepo.save(h);
        } catch (Exception e) {
            log.warn("Failed to record status history for application {}: {}",
                    app.getId(), e.getMessage());
        }
    }
}
