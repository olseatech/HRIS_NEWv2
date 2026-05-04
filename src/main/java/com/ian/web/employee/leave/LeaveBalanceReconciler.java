package com.ian.web.employee.leave;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs once at startup to verify that every leave_balance row correctly
 * reflects the sum of its ledger entries.
 *
 * For each (employee, leaveType, year) balance record it:
 *   1. Computes expected balance from the ledger (sum of signed day values)
 *   2. Compares to the stored balance
 *   3. Logs any discrepancy with WARN severity
 *   4. Silently corrects the stored balance so the system starts in a clean state
 *
 * This guards against balance drift caused by partial failures (e.g., balance
 * saved but ledger entry rolled back, or vice versa) in previous code versions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveBalanceReconciler implements ApplicationRunner {

    private final LeaveBalanceRepository balanceRepo;
    private final LeaveLedgerRepository  ledgerRepo;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            reconcile();
        } catch (Exception e) {
            // Never block application startup; log the error and continue.
            log.error("LeaveBalanceReconciler failed at startup — skipping reconciliation: {}", e.getMessage(), e);
        }
    }

    private void reconcile() {
        int year = LocalDate.now().getYear();
        List<LeaveBalance> balances = balanceRepo.findAll();
        int checked = 0, corrected = 0;

        for (LeaveBalance bal : balances) {
            if (bal.getBalanceYear() != year && bal.getBalanceYear() != year - 1) {
                continue; // Only reconcile current and previous year to keep runtime short
            }
            checked++;

            List<LeaveLedger> entries = ledgerRepo
                    .findByEmployeeIdAndLeaveTypeIdOrderByTransactionDateDesc(
                            bal.getEmployee().getId(), bal.getLeaveType().getId());

            double ledgerEarned = 0;
            double ledgerUsed   = 0;

            for (LeaveLedger e : entries) {
                if (e.getTransactionDate() == null) continue;
                if (e.getTransactionDate().getYear() != bal.getBalanceYear()) continue;

                double d = e.getDays();
                switch (e.getTransactionType()) {
                    case CREDIT:
                        ledgerEarned += d;
                        break;
                    case DEBIT:
                        // DEBIT days are stored as negative
                        ledgerUsed += Math.abs(d);
                        break;
                    case VOID:
                        // VOID reverses a DEBIT — subtract from used
                        ledgerUsed = Math.max(0, ledgerUsed - d);
                        break;
                    case ADJUSTMENT:
                        if (d > 0) ledgerEarned += d;
                        else       ledgerUsed   += Math.abs(d);
                        break;
                }
            }

            double expectedBalance = ledgerEarned - ledgerUsed;
            double storedBalance   = bal.getBalance();
            double tolerance       = 0.001;

            if (Math.abs(storedBalance - expectedBalance) > tolerance) {
                log.warn("Balance drift detected — Employee #{} ({}) | LeaveType: {} | Year: {} | "
                        + "Stored: {} | Expected from ledger: {} | Diff: {}. Auto-correcting.",
                        bal.getEmployee().getId(),
                        bal.getEmployee().getDisplayName(),
                        bal.getLeaveType().getLeaveCode(),
                        bal.getBalanceYear(),
                        storedBalance, expectedBalance,
                        String.format("%.3f", storedBalance - expectedBalance));

                bal.setTotalEarned(ledgerEarned);
                bal.setTotalUsed(ledgerUsed);
                bal.setBalance(expectedBalance);
                balanceRepo.save(bal);
                corrected++;
            }
        }

        if (corrected > 0) {
            log.warn("LeaveBalanceReconciler: checked {} balance records, corrected {}.", checked, corrected);
        } else {
            log.info("LeaveBalanceReconciler: checked {} balance records, all consistent.", checked);
        }
    }
}
