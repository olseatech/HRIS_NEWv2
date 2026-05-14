package com.ian.web.employee.leave;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Leave reports controller.
 *
 * Routes:
 *   GET  /leave-report                      — leave summary report page (admin)
 *   GET  /leave-card/{employeeId}/{year}     — individual leave card view (admin)
 *   GET  /leave-export-csv                  — download CSV of all applications for a year
 *   GET  /leave-balance-export-csv/{year}   — download CSV of all employee balances for a year
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LeaveReportController {

    private final LeaveApplicationRepository applicationRepo;
    private final LeaveBalanceRepository     balanceRepo;
    private final LeaveTypeRepository        leaveTypeRepo;
    private final EmployeeRepository         employeeRepository;
    private final LeaveService               leaveService;

    // -----------------------------------------------------------------------
    // LEAVE SUMMARY REPORT PAGE
    // -----------------------------------------------------------------------

    private static void noCache(HttpServletResponse res) {
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);
    }

    @GetMapping("/leave-report")
    public String reportPage(
            @RequestParam(value = "year",       required = false) Integer year,
            @RequestParam(value = "employeeId", required = false) Long    employeeId,
            @RequestParam(value = "leaveTypeId",required = false) Long    leaveTypeId,
            @RequestParam(value = "status",     required = false) String  status,
            Model model, HttpServletRequest request, HttpServletResponse response) {

        noCache(response);
        if (!isAdmin(request)) return "redirect:/dashboard";

        int selectedYear = (year != null) ? year : LocalDate.now().getYear();
        try {
            // Use JOIN FETCH to prevent N+1 and LazyInitializationException
            List<LeaveApplication> allYearApps = applicationRepo.findAllForYearFetched(selectedYear);

            // Apply optional filters
            List<LeaveApplication> applications = allYearApps;
            if (employeeId != null) {
                final Long fEmpId = employeeId;
                applications = applications.stream()
                        .filter(a -> a.getEmployee() != null
                                && fEmpId.equals(a.getEmployee().getId()))
                        .collect(Collectors.toList());
            }
            if (leaveTypeId != null) {
                final Long fLtId = leaveTypeId;
                applications = applications.stream()
                        .filter(a -> a.getLeaveType() != null
                                && fLtId.equals(a.getLeaveType().getId()))
                        .collect(Collectors.toList());
            }
            if (status != null && !status.isBlank()) {
                try {
                    LeaveApplication.LeaveStatus st = LeaveApplication.LeaveStatus.valueOf(status);
                    applications = applications.stream()
                            .filter(a -> a.getStatus() == st)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException ignored) { }
            }

            // Group applications by employee ID for the employee-centric table
            Map<Long, List<LeaveApplication>> appsByEmpId = allYearApps.stream()
                    .filter(a -> a.getEmployee() != null)
                    .collect(Collectors.groupingBy(
                            a -> a.getEmployee().getId(),
                            LinkedHashMap::new,
                            Collectors.toList()));

            // Per-employee summary stats (counts and days)
            Map<Long, Long>   pendingByEmpId     = new LinkedHashMap<>();
            Map<Long, Long>   approvedByEmpId    = new LinkedHashMap<>();
            Map<Long, Long>   disapprovedByEmpId = new LinkedHashMap<>();
            Map<Long, Long>   lwopByEmpId        = new LinkedHashMap<>();
            Map<Long, Double> daysUsedByEmpId    = new LinkedHashMap<>();

            appsByEmpId.forEach((empId, apps) -> {
                pendingByEmpId.put(empId, apps.stream()
                        .filter(a -> a.getStatus() == LeaveApplication.LeaveStatus.PENDING).count());
                approvedByEmpId.put(empId, apps.stream()
                        .filter(a -> a.getStatus() == LeaveApplication.LeaveStatus.APPROVED).count());
                disapprovedByEmpId.put(empId, apps.stream()
                        .filter(a -> a.getStatus() == LeaveApplication.LeaveStatus.DISAPPROVED).count());
                lwopByEmpId.put(empId, apps.stream()
                        .filter(a -> a.getStatus() == LeaveApplication.LeaveStatus.APPROVED
                                && !a.isWithPay()).count());
                daysUsedByEmpId.put(empId, apps.stream()
                        .filter(a -> a.getStatus() == LeaveApplication.LeaveStatus.APPROVED)
                        .mapToDouble(LeaveApplication::getNumberOfDays).sum());
            });

            List<Employee> employees = employeeRepository.findAllWithAssociationsFetched();

            // Aggregate totals for the stats row
            long totalPending     = pendingByEmpId.values().stream().mapToLong(Long::longValue).sum();
            long totalApproved    = approvedByEmpId.values().stream().mapToLong(Long::longValue).sum();
            long totalDisapproved = disapprovedByEmpId.values().stream().mapToLong(Long::longValue).sum();

            // Build division-name lookup from the eagerly-fetched application data.
            // Employee.division is LAZY; accessing it on a detached entity in Thymeleaf
            // (spring.jpa.open-in-view=false) throws LazyInitializationException mid-stream
            // even through the ?. safe-nav operator (proxy is non-null, getName() fails).
            // allYearApps was loaded with LEFT JOIN FETCH e.division, so it is safe.
            Map<Long, String> empDivisionNames = new LinkedHashMap<>();
            for (LeaveApplication a : allYearApps) {
                if (a.getEmployee() == null) continue;
                if (empDivisionNames.containsKey(a.getEmployee().getId())) continue;
                try {
                    String dn = a.getEmployee().getDivision() != null
                            ? a.getEmployee().getDivision().getDivisionName() : "";
                    empDivisionNames.put(a.getEmployee().getId(), dn);
                } catch (Exception ignored) {
                    empDivisionNames.put(a.getEmployee().getId(), "");
                }
            }
            // Employees with no apps this year still need an entry (safe empty fallback)
            for (Employee e : employees) {
                empDivisionNames.putIfAbsent(e.getId(), "");
            }

            model.addAttribute("applications",      applications);
            model.addAttribute("appsByEmpId",       appsByEmpId);
            model.addAttribute("pendingByEmpId",    pendingByEmpId);
            model.addAttribute("approvedByEmpId",   approvedByEmpId);
            model.addAttribute("disapprovedByEmpId",disapprovedByEmpId);
            model.addAttribute("lwopByEmpId",       lwopByEmpId);
            model.addAttribute("daysUsedByEmpId",   daysUsedByEmpId);
            model.addAttribute("totalPending",      totalPending);
            model.addAttribute("totalApproved",     totalApproved);
            model.addAttribute("totalDisapproved",  totalDisapproved);
            model.addAttribute("employees",         employees);
            model.addAttribute("empDivisionNames",  empDivisionNames);
            model.addAttribute("leaveTypes",        leaveTypeRepo.findByActiveTrueOrderBySortOrderAscLeaveNameAsc());
        } catch (Exception e) {
            log.error("Leave report page error", e);
            model.addAttribute("applications",      Collections.emptyList());
            model.addAttribute("appsByEmpId",       Collections.emptyMap());
            model.addAttribute("pendingByEmpId",    Collections.emptyMap());
            model.addAttribute("approvedByEmpId",   Collections.emptyMap());
            model.addAttribute("disapprovedByEmpId",Collections.emptyMap());
            model.addAttribute("lwopByEmpId",       Collections.emptyMap());
            model.addAttribute("daysUsedByEmpId",   Collections.emptyMap());
            model.addAttribute("totalPending",      0L);
            model.addAttribute("totalApproved",     0L);
            model.addAttribute("totalDisapproved",  0L);
            model.addAttribute("employees",         Collections.emptyList());
            model.addAttribute("empDivisionNames",  Collections.emptyMap());
            model.addAttribute("leaveTypes",        Collections.emptyList());
            model.addAttribute("uxmessage",
                new UXMessage("ERROR",
                    "DB error: " + e.getMessage()
                        + " — Run leave_migration.sql then restart."));
        }

        model.addAttribute("selectedYear",       selectedYear);
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("selectedLeaveTypeId",leaveTypeId);
        model.addAttribute("selectedStatus",     status);
        model.addAttribute("currentYear",        LocalDate.now().getYear());
        model.addAttribute("statuses",           LeaveApplication.LeaveStatus.values());
        return "employee/leave/leave-report";
    }

    // -----------------------------------------------------------------------
    // INDIVIDUAL LEAVE CARD (per employee per year)
    // -----------------------------------------------------------------------

    @GetMapping("/leave-card/{employeeId}/{year}")
    public String leaveCard(
            @PathVariable Long employeeId,
            @PathVariable int  year,
            Model model, HttpServletRequest request, HttpServletResponse response) {

        noCache(response);
        if (!isAdmin(request)) return "redirect:/dashboard";

        Employee employee = employeeRepository.findByIdFetched(employeeId).orElse(null);
        if (employee == null) return "redirect:/leave-report";

        List<LeaveBalance> balances = Collections.emptyList();
        try {
            balances = leaveService.getBalancesForEmployee(employeeId, year);
        } catch (Exception e) {
            log.warn("Failed to load balances for leave card emp={} year={}: {}",
                    employeeId, year, e.getMessage());
        }

        List<LeaveLedger> ledger = Collections.emptyList();
        try {
            ledger = leaveService.getLedgerForEmployeeFetched(employeeId);
        } catch (Exception e) {
            log.warn("Failed to load ledger for leave card emp={}: {}",
                    employeeId, e.getMessage());
        }

        List<LeaveApplication> applications = Collections.emptyList();
        try {
            applications = applicationRepo.findByEmployeeAndYearFetched(employeeId, year);
        } catch (Exception e) {
            log.warn("Failed to load applications for leave card emp={} year={}: {}",
                    employeeId, year, e.getMessage());
        }

        model.addAttribute("employee",     employee);
        model.addAttribute("year",         year);
        model.addAttribute("balances",     balances);
        model.addAttribute("ledger",       ledger);
        model.addAttribute("applications", applications);
        model.addAttribute("leaveTypes",   leaveTypeRepo.findByActiveTrueOrderBySortOrderAscLeaveNameAsc());
        return "employee/leave/leave-card";
    }

    // -----------------------------------------------------------------------
    // CSV EXPORT — ALL APPLICATIONS FOR A YEAR
    // -----------------------------------------------------------------------

    @GetMapping("/leave-export-csv")
        public void exportApplicationsCsv(
            @RequestParam(value = "year", required = false) Integer year,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (!isAdmin(request)) { response.setStatus(403); return; }

        int selectedYear = (year != null) ? year : LocalDate.now().getYear();
        List<LeaveApplication> applications = applicationRepo.findAllByOrderByAppliedDateTimeDesc()
                .stream()
                .filter(a -> a.getDateFrom() != null && a.getDateFrom().getYear() == selectedYear)
                .collect(java.util.stream.Collectors.toList());

        StringBuilder csv = new StringBuilder(1024);
        csv.append("Application No,Employee Name,Employee No,Division,")
           .append("Leave Type,Date From,Date To,Working Days,")
           .append("Leave Sub-Type,Leave Details,Reason,")
           .append("Status,With Pay,Commutation Requested,")
           .append("HRMO Cert Date,HRMO Name,Cert Days With Pay,Cert Days Without Pay,")
           .append("Supervisor Recommendation,Supervisor Name,Supervisor Date,Supervisor Remarks,")
           .append("Final Days With Pay,Final Days Without Pay,")
           .append("Approved By,Approved Date,Remarks,")
           .append("Filed On,Expected Return Date\n");

        for (LeaveApplication a : applications) {
            Employee employee = a.getEmployee();
            LeaveType leaveType = a.getLeaveType();
            // Division is accessed via safe try-catch: Employee.division may be a lazy proxy
            // on a detached entity (spring.jpa.open-in-view=false), causing LazyInitializationException
            // even when guarded by != null (the proxy is non-null but uninitialized).
            String division = "";
            try {
                if (employee != null && employee.getDivision() != null) {
                    division = employee.getDivision().getDivisionName();
                }
            } catch (Exception ignored) { }

            csv.append(csvField(a.getId())).append(',')
               .append(csvField(employee != null ? employee.getDisplayName() : "")).append(',')
               .append(csvField(employee != null ? employee.getEmpNo() : "")).append(',')
               .append(csvField(division)).append(',')
               .append(csvField(leaveType != null ? leaveType.getLeaveName() : "")).append(',')
               .append(csvField(a.getDateFrom())).append(',')
               .append(csvField(a.getDateTo())).append(',')
               .append(csvField(a.getNumberOfDays())).append(',')
               .append(csvField(a.getLeaveSubType())).append(',')
               .append(csvField(a.getLeaveDetails())).append(',')
               .append(csvField(a.getReason())).append(',')
               .append(csvField(a.getStatus())).append(',')
               .append(csvField(a.isWithPay() ? "Yes" : "No")).append(',')
               .append(csvField(a.isRequestedCommutation() ? "Yes" : "No")).append(',')
               .append(csvField(a.getHrmoCertDate())).append(',')
               .append(csvField(a.getHrmoName())).append(',')
               .append(csvField(a.getCertDaysWithPay())).append(',')
               .append(csvField(a.getCertDaysWithoutPay())).append(',')
               .append(csvField(a.getSupervisorRecommendation())).append(',')
               .append(csvField(a.getSupervisorName())).append(',')
               .append(csvField(a.getSupervisorActionDate())).append(',')
               .append(csvField(a.getSupervisorRemarks())).append(',')
               .append(csvField(a.getFinalDaysWithPay())).append(',')
               .append(csvField(a.getFinalDaysWithoutPay())).append(',')
               .append(csvField(a.getApprovedByName())).append(',')
               .append(csvField(a.getApprovedDate())).append(',')
               .append(csvField(a.getRemarks())).append(',')
               .append(csvField(a.getAppliedDateTime() != null
                   ? a.getAppliedDateTime().toLocalDate() : null)).append(',')
               .append(csvField(a.getExpectedReturnDate())).append('\n');
        }

        writeCsvResponse(response,
            "leave-applications-" + selectedYear + ".csv",
            csv.toString());
    }

    // -----------------------------------------------------------------------
    // CSV EXPORT — LEAVE BALANCES FOR ALL EMPLOYEES
    // -----------------------------------------------------------------------

    @GetMapping("/leave-balance-export-csv/{year}")
        public void exportBalancesCsv(
            @PathVariable int year,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (!isAdmin(request)) { response.setStatus(403); return; }

        List<Employee> employees = employeeRepository.findAllWithAssociationsFetched();
        StringBuilder csv = new StringBuilder(1024);
        csv.append("Employee Name,Employee No,Division,Leave Type,")
           .append("Year,Total Earned,Total Used,Balance\n");

        for (Employee emp : employees) {
            List<LeaveBalance> balances;
            try {
                balances = leaveService.getBalancesForEmployee(emp.getId(), year);
            } catch (Exception e) {
                log.warn("Failed to load balances for employee {} in year {}: {}",
                        emp.getId(), year, e.getMessage());
                continue;
            }

            String division = "";
            try {
                division = emp.getDivision() != null
                        ? emp.getDivision().getDivisionName() : "";
            } catch (Exception ignored) { }
            for (LeaveBalance b : balances) {
                LeaveType leaveType = b.getLeaveType();
                csv.append(csvField(emp.getDisplayName())).append(',')
                   .append(csvField(emp.getEmpNo())).append(',')
                   .append(csvField(division)).append(',')
                   .append(csvField(leaveType != null ? leaveType.getLeaveName() : "")).append(',')
                   .append(csvField(b.getBalanceYear())).append(',')
                   .append(csvField(b.getTotalEarned())).append(',')
                   .append(csvField(b.getTotalUsed())).append(',')
                   .append(csvField(b.getBalance())).append('\n');
            }
        }

        writeCsvResponse(response,
                "leave-balances-" + year + ".csv",
                csv.toString());
    }

    // -----------------------------------------------------------------------
    // PDF EXPORT — CS FORM NO. 6 (APPLICATION FOR LEAVE)
    // -----------------------------------------------------------------------

    @GetMapping("/viewLeaveApplicationPdf/{leaveId}")
    public void viewLeaveApplicationPdf(
            @PathVariable Long leaveId,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (!isAdmin(request)) { response.setStatus(403); return; }

        Optional<LeaveApplication> opt = applicationRepo.findById(leaveId);
        if (opt.isEmpty()) { response.setStatus(404); return; }

        LeaveApplication app   = opt.get();
        Employee         emp   = app.getEmployee();
        LeaveType        lt    = app.getLeaveType();
        String           ltName = lt != null ? lt.getLeaveName() : "";

        // ── 1. Employee info ─────────────────────────────────────────────────
        String officeDept = "";
        try { if (emp.getDivision() != null) officeDept = emp.getDivision().getDivisionName(); }
        catch (Exception ignored) {}

        String position = "";
        try { if (emp.getPositionTitle() != null) position = emp.getPositionTitle().getPositionTitleName(); }
        catch (Exception ignored) {}

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String dateOfFiling = app.getAppliedDateTime() != null
                ? app.getAppliedDateTime().toLocalDate().format(dateFmt) : "";

        String inclusiveDates = "";
        if (app.getDateFrom() != null && app.getDateTo() != null) {
            inclusiveDates = app.getDateFrom().format(dateFmt)
                    + " – " + app.getDateTo().format(dateFmt);
        }

        // ── 2. Leave type checkboxes ─────────────────────────────────────────
        boolean chkVacation          = ltName.equalsIgnoreCase("Vacation Leave");
        boolean chkMandatory         = ltName.toLowerCase().contains("mandatory") || ltName.toLowerCase().contains("forced");
        boolean chkSick              = ltName.equalsIgnoreCase("Sick Leave");
        boolean chkMaternity         = ltName.toLowerCase().contains("maternity");
        boolean chkPaternity         = ltName.toLowerCase().contains("paternity");
        boolean chkSpecialPrivilege  = ltName.toLowerCase().contains("special privilege");
        boolean chkSoloParent        = ltName.toLowerCase().contains("solo parent");
        boolean chkStudy             = ltName.toLowerCase().contains("study");
        boolean chkVAWC              = ltName.toLowerCase().contains("vawc");
        boolean chkRehabilitation    = ltName.toLowerCase().contains("rehabilit");
        boolean chkWomen             = ltName.toLowerCase().contains("women");
        boolean chkCalamity          = ltName.toLowerCase().contains("calamity");
        boolean chkOthers            = !chkVacation && !chkMandatory && !chkSick
                && !chkMaternity && !chkPaternity && !chkSpecialPrivilege
                && !chkSoloParent && !chkStudy && !chkVAWC && !chkRehabilitation
                && !chkWomen && !chkCalamity;
        String othersSpec = chkOthers ? ltName : "";

        // ── 3. Sub-type checkboxes (Section 6B) ──────────────────────────────
        String subType   = app.getLeaveSubType() != null ? app.getLeaveSubType() : "";
        String subDetail = app.getLeaveDetails()  != null ? app.getLeaveDetails()  : "";

        boolean chkWithinPhilippines = subType.equalsIgnoreCase("WITHIN_PHILIPPINES");
        String  withinPhilippinesSpec = chkWithinPhilippines ? subDetail : "";
        boolean chkAbroad            = subType.equalsIgnoreCase("ABROAD");
        String  abroadSpec           = chkAbroad ? subDetail : "";
        boolean chkInHospital        = subType.equalsIgnoreCase("IN_HOSPITAL");
        String  inHospitalSpec       = chkInHospital ? subDetail : "";
        boolean chkOutPatient        = subType.equalsIgnoreCase("OUT_PATIENT");
        String  outPatientSpec       = chkOutPatient ? subDetail : "";
        boolean chkMastersDegree     = subType.equalsIgnoreCase("MASTERS_DEGREE");
        boolean chkBoardExam         = subType.equalsIgnoreCase("BOARD_EXAM");
        boolean chkMonetization      = subType.equalsIgnoreCase("MONETIZATION");
        boolean chkTerminalLeave     = subType.equalsIgnoreCase("TERMINAL_LEAVE");
        String  womenSpec            = chkWomen ? subDetail : "";

        // ── 4. Leave credits — 7A ────────────────────────────────────────────
        int currentYear = app.getDateFrom() != null ? app.getDateFrom().getYear() : LocalDate.now().getYear();
        List<LeaveBalance> balances = Collections.emptyList();
        try { balances = leaveService.getBalancesForEmployee(emp.getId(), currentYear); }
        catch (Exception ignored) {}

        String vacAsOf = "", vacTotalEarned = "", vacLessThisApp = "", vacBalance = "";
        String slAsOf  = "", slTotalEarned  = "", slLessThisApp  = "", slBalance  = "";

        for (LeaveBalance b : balances) {
            if (b.getLeaveType() == null) continue;
            String bName = b.getLeaveType().getLeaveName();
            if (bName != null && bName.equalsIgnoreCase("Vacation Leave")) {
                vacAsOf        = String.valueOf(b.getBalanceYear());
                vacTotalEarned = fmt(b.getTotalEarned());
                vacLessThisApp = chkVacation ? fmt(app.getNumberOfDays()) : "0.000";
                vacBalance     = fmt(b.getBalance());
            } else if (bName != null && bName.equalsIgnoreCase("Sick Leave")) {
                slAsOf        = String.valueOf(b.getBalanceYear());
                slTotalEarned = fmt(b.getTotalEarned());
                slLessThisApp = chkSick ? fmt(app.getNumberOfDays()) : "0.000";
                slBalance     = fmt(b.getBalance());
            }
        }

        // ── 5. Recommendation / Approval — 7B / 7C / 7D ─────────────────────
        String supRec = app.getSupervisorRecommendation() != null ? app.getSupervisorRecommendation() : "";
        boolean chkForApproval    = supRec.contains("APPROVED") && !supRec.contains("DISAPPROVED");
        boolean chkForDisapproval = supRec.equalsIgnoreCase("DISAPPROVED");

        String daysWithPay    = app.getFinalDaysWithPay()    != null ? fmt(app.getFinalDaysWithPay())    : "";
        String daysWithoutPay = app.getFinalDaysWithoutPay() != null ? fmt(app.getFinalDaysWithoutPay()) : "";

        boolean isDisapproved = app.getStatus() == LeaveApplication.LeaveStatus.DISAPPROVED;
        String disapprovedDueTo = isDisapproved && app.getRemarks() != null ? app.getRemarks() : "";

        String applicantSignDate = app.getAppliedDateTime() != null
                ? app.getAppliedDateTime().toLocalDate().format(dateFmt) : "";

        // ── 6. Build parameter map ────────────────────────────────────────────
        Map<String, Object> params = new HashMap<>();
        params.put("officeDepartment",       officeDept);
        params.put("lastName",               emp.getLastName()   != null ? emp.getLastName()   : "");
        params.put("firstName",              emp.getFirstName()  != null ? emp.getFirstName()  : "");
        params.put("middleName",             emp.getMiddleName() != null ? emp.getMiddleName() : "");
        params.put("dateOfFiling",           dateOfFiling);
        params.put("position",               position);
        params.put("salary",                 "");

        params.put("chkVacation",            chkVacation);
        params.put("chkMandatory",           chkMandatory);
        params.put("chkSick",                chkSick);
        params.put("chkMaternity",           chkMaternity);
        params.put("chkPaternity",           chkPaternity);
        params.put("chkSpecialPrivilege",    chkSpecialPrivilege);
        params.put("chkSoloParent",          chkSoloParent);
        params.put("chkStudy",               chkStudy);
        params.put("chkVAWC",                chkVAWC);
        params.put("chkRehabilitation",      chkRehabilitation);
        params.put("chkWomen",               chkWomen);
        params.put("chkCalamity",            chkCalamity);
        params.put("chkOthers",              chkOthers);
        params.put("othersSpec",             othersSpec);

        params.put("chkWithinPhilippines",   chkWithinPhilippines);
        params.put("withinPhilippinesSpec",  withinPhilippinesSpec);
        params.put("chkAbroad",              chkAbroad);
        params.put("abroadSpec",             abroadSpec);
        params.put("chkInHospital",          chkInHospital);
        params.put("inHospitalSpec",         inHospitalSpec);
        params.put("chkOutPatient",          chkOutPatient);
        params.put("outPatientSpec",         outPatientSpec);
        params.put("womenSpec",              womenSpec);
        params.put("chkMastersDegree",       chkMastersDegree);
        params.put("chkBoardExam",           chkBoardExam);
        params.put("chkMonetization",        chkMonetization);
        params.put("chkTerminalLeave",       chkTerminalLeave);

        params.put("inclusiveDates",         inclusiveDates);
        params.put("numberOfWorkingDays",    fmt(app.getNumberOfDays()));
        params.put("chkCommutationRequested",    app.isRequestedCommutation());
        params.put("chkCommutationNotRequested", !app.isRequestedCommutation());

        params.put("vacAsOf",         vacAsOf);
        params.put("vacTotalEarned",  vacTotalEarned);
        params.put("vacLessThisApp",  vacLessThisApp);
        params.put("vacBalance",      vacBalance);
        params.put("slAsOf",          slAsOf);
        params.put("slTotalEarned",   slTotalEarned);
        params.put("slLessThisApp",   slLessThisApp);
        params.put("slBalance",       slBalance);
        params.put("certifiedBy",     app.getHrmoName()      != null ? app.getHrmoName()      : "");
        params.put("certifiedByTitle", "HRMO");

        params.put("chkForApproval",    chkForApproval);
        params.put("chkForDisapproval", chkForDisapproval);
        params.put("disapprovalReason", app.getSupervisorRemarks() != null ? app.getSupervisorRemarks() : "");
        params.put("supervisorName",    app.getSupervisorName()    != null ? app.getSupervisorName()    : "");
        params.put("supervisorTitle",   "HR Admin");

        params.put("daysWithPay",          daysWithPay);
        params.put("daysWithoutPay",       daysWithoutPay);
        params.put("othersApprovalSpec",   "");
        params.put("disapprovedDueTo",     disapprovedDueTo);
        params.put("approvedByName",       app.getApprovedByName() != null ? app.getApprovedByName() : "");
        params.put("approvedByTitle",      "Authorized Official");
        params.put("applicantSignDate",    applicantSignDate);

        // ── 7. Compile JRXML and stream PDF ──────────────────────────────────
        try {
            InputStream jrxmlStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("jasper/reports/LeaveApplication.jrxml");
            if (jrxmlStream == null) {
                throw new RuntimeException("JRXML not found: jasper/reports/LeaveApplication.jrxml");
            }
            JasperReport  jasperReport = JasperCompileManager.compileReport(jrxmlStream);
            JasperPrint   jasperPrint  = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "inline; filename=\"LeaveApplication-" + leaveId + ".pdf\"");
            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("Failed to generate leave application PDF for leaveId={}", leaveId, e);
            response.setStatus(500);
        }
    }

    /** Format a double as a leave-days string with 3 decimal places. */
    private String fmt(double value) {
        return String.format("%.3f", value);
    }

    // -----------------------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------------------

    private Employee getActor(HttpServletRequest request) {
        Employee actor = (Employee) request.getSession().getAttribute("actorObj");
        if (actor == null) {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Employee) {
                actor = (Employee) auth.getPrincipal();
            }
        }
        return actor;
    }

    private boolean isAdmin(HttpServletRequest request) {
        Employee actor = getActor(request);
        return actor != null && actor.getUserType() != null
                && "ROLE_ADMIN".equals(actor.getUserType());
    }

    private String csvField(Object value) {
        if (value == null) return "";
        String s = value.toString().replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }

    private void writeCsvResponse(HttpServletResponse response, String filename, String csvBody) {
        try {
            byte[] bytes = csvBody.getBytes(StandardCharsets.UTF_8);
            response.resetBuffer();
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + filename + "\"");
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.flushBuffer();
        } catch (Exception e) {
            log.error("Failed to write CSV response: {}", e.getMessage(), e);
        }
    }
}
