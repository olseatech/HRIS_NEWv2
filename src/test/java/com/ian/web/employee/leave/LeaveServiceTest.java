package com.ian.web.employee.leave;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ian.web.employee.Employee;
import com.ian.web.employee.leave.LeaveLedger.TransactionType;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock LeaveApplicationRepository   applicationRepo;
    @Mock LeaveBalanceRepository       balanceRepo;
    @Mock LeaveLedgerRepository        ledgerRepo;
    @Mock LeaveTypeRepository          leaveTypeRepo;
    @Mock LeaveStatusHistoryRepository historyRepo;
    @Mock PublicHolidayRepository      holidayRepo;
    @Mock LeaveEmailService            emailService;

    @InjectMocks
    LeaveService leaveService;

    private Employee employee;
    private LeaveType vlType;
    private LeaveType lwopType;
    private LeaveType mlType;
    private LeaveType plType;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Juan");
        employee.setLastName("dela Cruz");
        employee.setGender("MALE");

        vlType = new LeaveType();
        vlType.setId(1L);
        vlType.setLeaveCode("VL");
        vlType.setLeaveName("Vacation Leave");
        vlType.setAccrualPerMonth(1.25);
        vlType.setCarryOverMax(30);
        vlType.setLwopType(false);
        vlType.setActive(true);

        lwopType = new LeaveType();
        lwopType.setId(2L);
        lwopType.setLeaveCode("LWOP");
        lwopType.setLeaveName("Leave Without Pay");
        lwopType.setLwopType(true);
        lwopType.setActive(true);

        mlType = new LeaveType();
        mlType.setId(3L);
        mlType.setLeaveCode("ML");
        mlType.setLeaveName("Maternity Leave");
        mlType.setLwopType(false);
        mlType.setActive(true);

        plType = new LeaveType();
        plType.setId(4L);
        plType.setLeaveCode("PL");
        plType.setLeaveName("Paternity Leave");
        plType.setLwopType(false);
        plType.setActive(true);

        // By default no public holidays (lenient because only used in countWorkingDays_excludesHoliday)
        lenient().when(holidayRepo.findExcludedHolidaysBetween(any(), any()))
                .thenReturn(Collections.emptyList());

        // History save always succeeds (lenient because not used in all tests)
        lenient().when(historyRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        // Email service is async — just verify calls, no real sending (lenient because not all tests verify these)
        lenient().doNothing().when(emailService).notifyApplied(any());
        lenient().doNothing().when(emailService).notifyEndorsed(any());
        lenient().doNothing().when(emailService).notifyApproved(any());
        lenient().doNothing().when(emailService).notifyDisapproved(any());
        lenient().doNothing().when(emailService).notifyReturned(any());
        lenient().doNothing().when(emailService).notifyCancelled(any());
    }

    // -----------------------------------------------------------------------
    // countWorkingDays
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("countWorkingDays: single weekday = 1")
    void countWorkingDays_singleWeekday() {
        LocalDate monday = LocalDate.of(2025, 5, 5);
        assertThat(leaveService.countWorkingDays(monday, monday)).isEqualTo(1.0);
    }

    @Test
    @DisplayName("countWorkingDays: full week Mon–Fri = 5")
    void countWorkingDays_fullWeek() {
        LocalDate mon = LocalDate.of(2025, 5, 5);
        LocalDate fri = LocalDate.of(2025, 5, 9);
        assertThat(leaveService.countWorkingDays(mon, fri)).isEqualTo(5.0);
    }

    @Test
    @DisplayName("countWorkingDays: weekend only = 0")
    void countWorkingDays_weekendOnly() {
        LocalDate sat = LocalDate.of(2025, 5, 10);
        LocalDate sun = LocalDate.of(2025, 5, 11);
        assertThat(leaveService.countWorkingDays(sat, sun)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("countWorkingDays: excludes public holiday")
    void countWorkingDays_excludesHoliday() {
        LocalDate mon = LocalDate.of(2025, 6, 12); // Independence Day
        LocalDate fri = LocalDate.of(2025, 6, 12);
        PublicHoliday holiday = new PublicHoliday();
        holiday.setHolidayDate(mon);
        holiday.setExcludeFromLeave(true);
        when(holidayRepo.findExcludedHolidaysBetween(mon, fri))
                .thenReturn(java.util.List.of(holiday));

        assertThat(leaveService.countWorkingDays(mon, fri)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("countWorkingDays: null dates return 0")
    void countWorkingDays_nullDates() {
        assertThat(leaveService.countWorkingDays(null, null)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("countWorkingDays: dateTo before dateFrom returns 0")
    void countWorkingDays_invertedRange() {
        LocalDate from = LocalDate.of(2025, 5, 9);
        LocalDate to   = LocalDate.of(2025, 5, 5);
        assertThat(leaveService.countWorkingDays(from, to)).isEqualTo(0.0);
    }

    // -----------------------------------------------------------------------
    // applyLeave
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("applyLeave: sets PENDING status and saves")
    void applyLeave_setsPendingAndSaves() {
        LeaveApplication app = buildApplication(vlType,
                LocalDate.now(), LocalDate.now());
        when(applicationRepo.findOverlapping(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(applicationRepo.save(any())).thenAnswer(i -> {
            LeaveApplication a = i.getArgument(0);
            a.setId(99L);
            return a;
        });

        LeaveApplication result = leaveService.applyLeave(app);

        assertThat(result.getStatus()).isEqualTo(LeaveApplication.LeaveStatus.PENDING);
        assertThat(result.isLedgerPosted()).isFalse();
        verify(applicationRepo).save(any());
        verify(emailService).notifyApplied(any());
    }

    @Test
    @DisplayName("applyLeave: rejects when dateFrom is more than 3 months in the past")
    void applyLeave_rejectsStaleDate() {
        LeaveApplication app = buildApplication(vlType,
                LocalDate.now().minusMonths(4), LocalDate.now().minusMonths(4));
        assertThatThrownBy(() -> leaveService.applyLeave(app))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 months");
    }

    @Test
    @DisplayName("applyLeave: rejects overlapping application")
    void applyLeave_rejectsOverlap() {
        LeaveApplication app = buildApplication(vlType,
                LocalDate.now(), LocalDate.now().plusDays(2));
        LeaveApplication existing = buildApplication(vlType,
                LocalDate.now(), LocalDate.now().plusDays(1));
        existing.setId(5L);
        existing.setStatus(LeaveApplication.LeaveStatus.PENDING);

        when(applicationRepo.findOverlapping(any(), any(), any(), any()))
                .thenReturn(java.util.List.of(existing));

        assertThatThrownBy(() -> leaveService.applyLeave(app))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Overlapping");
    }

    // -----------------------------------------------------------------------
    // Gender eligibility
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("applyLeave: male employee filing ML throws")
    void applyLeave_maleFilingMaternity_throws() {
        employee.setGender("MALE");
        LeaveApplication app = buildApplication(mlType, LocalDate.now(), LocalDate.now());
        // No findOverlapping stub — validateGenderEligibility throws before overlap check is reached

        assertThatThrownBy(() -> leaveService.applyLeave(app))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("female employees");
    }

    @Test
    @DisplayName("applyLeave: female employee filing PL throws")
    void applyLeave_femaleFilingPaternity_throws() {
        employee.setGender("FEMALE");
        LeaveApplication app = buildApplication(plType, LocalDate.now(), LocalDate.now());
        // No findOverlapping stub — validateGenderEligibility throws before overlap check is reached

        assertThatThrownBy(() -> leaveService.applyLeave(app))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("male employees");
    }

    @Test
    @DisplayName("applyLeave: male employee filing PL succeeds")
    void applyLeave_maleFilingPaternity_succeeds() {
        employee.setGender("MALE");
        LeaveApplication app = buildApplication(plType, LocalDate.now(), LocalDate.now());
        when(applicationRepo.findOverlapping(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(applicationRepo.save(any())).thenAnswer(i -> {
            LeaveApplication a = i.getArgument(0);
            a.setId(10L);
            return a;
        });

        assertThatCode(() -> leaveService.applyLeave(app)).doesNotThrowAnyException();
    }

    // -----------------------------------------------------------------------
    // endorseLeave
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("endorseLeave: PENDING → ENDORSED")
    void endorseLeave_pendingToEndorsed() {
        LeaveApplication app = pendingApplication();
        when(applicationRepo.findById(1L)).thenReturn(Optional.of(app));
        when(applicationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        LeaveApplication result = leaveService.endorseLeave(
                1L, 2L, "HR Admin", "APPROVED_WITH_PAY", null, 5.0, 0.0);

        assertThat(result.getStatus()).isEqualTo(LeaveApplication.LeaveStatus.ENDORSED);
        verify(emailService).notifyEndorsed(any());
    }

    @Test
    @DisplayName("endorseLeave: non-PENDING application throws")
    void endorseLeave_nonPending_throws() {
        LeaveApplication app = pendingApplication();
        app.setStatus(LeaveApplication.LeaveStatus.ENDORSED);
        when(applicationRepo.findById(1L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> leaveService.endorseLeave(
                1L, 2L, "HR", "APPROVED_WITH_PAY", null, 5.0, 0.0))
                .isInstanceOf(IllegalStateException.class);
    }

    // -----------------------------------------------------------------------
    // approveLeave
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("approveLeave: deducts balance and posts DEBIT ledger entry")
    void approveLeave_deductsBalance() {
        LeaveApplication app = pendingApplication();
        app.setNumberOfDays(3.0);
        when(applicationRepo.findById(1L)).thenReturn(Optional.of(app));

        LeaveBalance balance = new LeaveBalance();
        balance.setTotalEarned(10.0);
        balance.setTotalUsed(0.0);
        balance.setBalance(10.0);
        when(balanceRepo.findByEmployeeIdAndLeaveTypeIdAndBalanceYear(any(), any(), anyInt()))
                .thenReturn(Optional.of(balance));
        when(balanceRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ledgerRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applicationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        LeaveApplication result = leaveService.approveLeave(1L, 2L, "HR Admin", null);

        assertThat(result.getStatus()).isEqualTo(LeaveApplication.LeaveStatus.APPROVED);
        assertThat(result.isWithPay()).isTrue();
        assertThat(result.isLedgerPosted()).isTrue();
        assertThat(balance.getBalance()).isEqualTo(7.0);

        ArgumentCaptor<LeaveLedger> ledgerCaptor = ArgumentCaptor.forClass(LeaveLedger.class);
        verify(ledgerRepo).save(ledgerCaptor.capture());
        assertThat(ledgerCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.DEBIT);
        assertThat(ledgerCaptor.getValue().getDays()).isEqualTo(-3.0);

        verify(emailService).notifyApproved(any());
    }

    @Test
    @DisplayName("approveLeave: insufficient balance → LWOP, no ledger DEBIT")
    void approveLeave_insufficientBalance_setsLwop() {
        LeaveApplication app = pendingApplication();
        app.setNumberOfDays(5.0);
        when(applicationRepo.findById(1L)).thenReturn(Optional.of(app));

        LeaveBalance balance = new LeaveBalance();
        balance.setBalance(2.0);
        balance.setTotalEarned(2.0);
        balance.setTotalUsed(0.0);
        when(balanceRepo.findByEmployeeIdAndLeaveTypeIdAndBalanceYear(any(), any(), anyInt()))
                .thenReturn(Optional.of(balance));
        when(applicationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        LeaveApplication result = leaveService.approveLeave(1L, 2L, "HR Admin", null);

        assertThat(result.isWithPay()).isFalse();
        assertThat(result.isLedgerPosted()).isFalse();
        verify(ledgerRepo, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // disapproveLeave
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("disapproveLeave: no balance impact, sends notification")
    void disapproveLeave_noBalanceImpact() {
        LeaveApplication app = pendingApplication();
        when(applicationRepo.findById(1L)).thenReturn(Optional.of(app));
        when(applicationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        LeaveApplication result = leaveService.disapproveLeave(1L, 2L, "HR Admin", "Insufficient docs");

        assertThat(result.getStatus()).isEqualTo(LeaveApplication.LeaveStatus.DISAPPROVED);
        verify(balanceRepo, never()).save(any());
        verify(ledgerRepo, never()).save(any());
        verify(emailService).notifyDisapproved(any());
    }

    // -----------------------------------------------------------------------
    // cancelLeave
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("cancelLeave: PENDING → CANCELLED, no ledger reversal")
    void cancelLeave_pendingNoBallanceImpact() {
        LeaveApplication app = pendingApplication();
        when(applicationRepo.findById(1L)).thenReturn(Optional.of(app));
        when(applicationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        LeaveApplication result = leaveService.cancelLeave(1L, 10L, "Juan", "Changed plans");

        assertThat(result.getStatus()).isEqualTo(LeaveApplication.LeaveStatus.CANCELLED);
        assertThat(result.getCancelledById()).isEqualTo(10L);
        assertThat(result.getCancelledByName()).isEqualTo("Juan");
        assertThat(result.getCancelReason()).isEqualTo("Changed plans");
        verify(ledgerRepo, never()).save(any());
        verify(emailService).notifyCancelled(any());
    }

    @Test
    @DisplayName("cancelLeave: APPROVED with pay → reverses DEBIT with VOID entry")
    void cancelLeave_approvedWithPay_reversesBalance() {
        LeaveApplication app = pendingApplication();
        app.setStatus(LeaveApplication.LeaveStatus.APPROVED);
        app.setWithPay(true);
        app.setLedgerPosted(true);
        app.setNumberOfDays(3.0);
        when(applicationRepo.findById(1L)).thenReturn(Optional.of(app));

        LeaveBalance balance = new LeaveBalance();
        balance.setTotalEarned(10.0);
        balance.setTotalUsed(3.0);
        balance.setBalance(7.0);
        when(balanceRepo.findByEmployeeIdAndLeaveTypeIdAndBalanceYear(any(), any(), anyInt()))
                .thenReturn(Optional.of(balance));
        when(balanceRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ledgerRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applicationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        leaveService.cancelLeave(1L, 2L, "HR Admin", null);

        assertThat(balance.getBalance()).isEqualTo(10.0);
        ArgumentCaptor<LeaveLedger> ledgerCaptor = ArgumentCaptor.forClass(LeaveLedger.class);
        verify(ledgerRepo).save(ledgerCaptor.capture());
        assertThat(ledgerCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.VOID);
        assertThat(ledgerCaptor.getValue().getDays()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("cancelLeave: DISAPPROVED application throws")
    void cancelLeave_disapproved_throws() {
        LeaveApplication app = pendingApplication();
        app.setStatus(LeaveApplication.LeaveStatus.DISAPPROVED);
        when(applicationRepo.findById(1L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> leaveService.cancelLeave(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("disapproved");
    }

    // -----------------------------------------------------------------------
    // returnForCorrection
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("returnForCorrection: PENDING → RETURNED, sends notification")
    void returnForCorrection_pendingToReturned() {
        LeaveApplication app = pendingApplication();
        when(applicationRepo.findById(1L)).thenReturn(Optional.of(app));
        when(applicationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        LeaveApplication result = leaveService.returnForCorrection(
                1L, 2L, "HR Admin", "Missing medical cert");

        assertThat(result.getStatus()).isEqualTo(LeaveApplication.LeaveStatus.RETURNED);
        verify(emailService).notifyReturned(any());
    }

    // -----------------------------------------------------------------------
    // resubmitLeave
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("resubmitLeave: RETURNED → PENDING, clears endorsement data")
    void resubmitLeave_clearsEndorsement() {
        LeaveApplication app = pendingApplication();
        app.setStatus(LeaveApplication.LeaveStatus.RETURNED);
        app.setSupervisorId(5L);
        app.setSupervisorName("HR");
        app.setHrmoId(5L);
        when(applicationRepo.findById(1L)).thenReturn(Optional.of(app));
        when(applicationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        LeaveApplication result = leaveService.resubmitLeave(
                1L, employee.getId(), "Corrected reason", null, null);

        assertThat(result.getStatus()).isEqualTo(LeaveApplication.LeaveStatus.PENDING);
        assertThat(result.getSupervisorId()).isNull();
        assertThat(result.getHrmoId()).isNull();
    }

    @Test
    @DisplayName("resubmitLeave: wrong employee throws SecurityException")
    void resubmitLeave_wrongEmployee_throws() {
        LeaveApplication app = pendingApplication();
        app.setStatus(LeaveApplication.LeaveStatus.RETURNED);
        when(applicationRepo.findById(1L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> leaveService.resubmitLeave(1L, 999L, null, null, null))
                .isInstanceOf(SecurityException.class);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private LeaveApplication buildApplication(LeaveType type, LocalDate from, LocalDate to) {
        LeaveApplication app = new LeaveApplication();
        app.setEmployee(employee);
        app.setLeaveType(type);
        app.setDateFrom(from);
        app.setDateTo(to);
        return app;
    }

    private LeaveApplication pendingApplication() {
        LeaveApplication app = buildApplication(vlType,
                LocalDate.now(), LocalDate.now().plusDays(4));
        app.setId(1L);
        app.setStatus(LeaveApplication.LeaveStatus.PENDING);
        app.setNumberOfDays(5.0);
        app.setLedgerPosted(false);
        return app;
    }
}
