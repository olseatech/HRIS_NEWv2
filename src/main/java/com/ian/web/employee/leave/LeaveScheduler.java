package com.ian.web.employee.leave;

import java.time.LocalDate;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled tasks for leave management:
 *   - Monthly accrual on the 1st of each month at 06:00 (Asia/Manila)
 *   - Year-end carry-over on January 1st at 07:00
 *   - Annual balance reset for leave types with annualResetMonth set (e.g. SPL)
 *
 * @EnableScheduling must be active (added to HrispWebApplication or any @Configuration).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveScheduler {

    private final LeaveService          leaveService;
    private final LeaveBalanceRepository balanceRepo;
    private final LeaveTypeRepository    leaveTypeRepo;
    private final EmployeeRepository     employeeRepository;

    // -----------------------------------------------------------------------
    // Monthly accrual — 1st of each month, 06:00 Asia/Manila
    // -----------------------------------------------------------------------

    @Scheduled(cron = "0 0 6 1 * *", zone = "Asia/Manila")
    public void monthlyAccrual() {
        int year  = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        log.info("LeaveScheduler: running monthly accrual for {}-{}", year, month);
        accrueAllEmployees(year, month);
        log.info("LeaveScheduler: monthly accrual complete.");
    }

    /** Manual trigger: accrue a specific year/month for ALL employees. */
    public void accrueAllEmployees(int year, int month) {
        List<Employee> employees = employeeRepository.findAll();
        int count = 0;
        for (Employee emp : employees) {
            try {
                leaveService.accrueMonthlyLeave(emp, year, month);
                count++;
            } catch (Exception e) {
                log.warn("Accrual skipped for employee {} ({}): {}",
                        emp.getId(), emp.getDisplayName(), e.getMessage());
            }
        }
        log.info("Accrued leave for {}/{} employees for {}-{}", count, employees.size(), year, month);
    }

    /** Backfill: accrue Jan → current month for all employees (use on fresh installs). */
    public void backfillCurrentYear() {
        int year         = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        log.info("LeaveScheduler: backfilling Jan–{} {} for all employees", currentMonth, year);
        for (int m = 1; m <= currentMonth; m++) {
            accrueAllEmployees(year, m);
        }
        log.info("LeaveScheduler: backfill complete.");
    }

    // -----------------------------------------------------------------------
    // Year-end carry-over — January 1st, 07:00 Asia/Manila
    // Caps unused VL/SL balance to carryOverMax and credits the new year.
    // -----------------------------------------------------------------------

    @Scheduled(cron = "0 0 7 1 1 *", zone = "Asia/Manila")
    public void yearEndCarryOver() {
        int newYear  = LocalDate.now().getYear();
        int prevYear = newYear - 1;
        log.info("LeaveScheduler: running year-end carry-over from {} to {}", prevYear, newYear);
        List<Employee> employees = employeeRepository.findAll();
        int count = 0;
        for (Employee emp : employees) {
            try {
                leaveService.carryOver(emp, prevYear, newYear);
                count++;
            } catch (Exception e) {
                log.warn("Carry-over skipped for employee {} ({}): {}",
                        emp.getId(), emp.getDisplayName(), e.getMessage());
            }
        }
        log.info("LeaveScheduler: carry-over complete for {}/{} employees.", count, employees.size());
    }

    // -----------------------------------------------------------------------
    // Annual balance reset — January 1st, 08:00 Asia/Manila
    // Resets balance to 0 for leave types with annualResetMonth = current month
    // (e.g. SPL resets every January).
    // -----------------------------------------------------------------------

    @Scheduled(cron = "0 0 8 1 1 *", zone = "Asia/Manila")
    public void annualBalanceReset() {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear  = LocalDate.now().getYear();
        List<LeaveType> types = leaveTypeRepo.findByActiveTrueOrderBySortOrderAscLeaveNameAsc();

        for (LeaveType type : types) {
            if (type.getAnnualResetMonth() == null || type.getAnnualResetMonth() != currentMonth) {
                continue;
            }
            log.info("LeaveScheduler: resetting annual balance for leave type {} ({})",
                    type.getLeaveCode(), type.getLeaveName());
            List<LeaveBalance> balances = balanceRepo.findByLeaveTypeIdAndBalanceYear(
                    type.getId(), currentYear);
            for (LeaveBalance bal : balances) {
                bal.setTotalEarned(0);
                bal.setTotalUsed(0);
                bal.setBalance(0);
                balanceRepo.save(bal);
            }
            log.info("LeaveScheduler: reset {} balance records for {}.", balances.size(), type.getLeaveCode());
        }
    }
}

