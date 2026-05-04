package com.ian.web.employee.leave;

import java.time.LocalDate;
import javax.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import com.ian.web.employee.Employee;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

/**
 * Immutable audit trail of every leave balance movement per employee/type.
 * CREDIT entries: accrual, carry-over, manual adjustment.
 * DEBIT  entries: approved leave consumed, LWOP days.
 * VOID   entries: reversal when leave is cancelled after approval.
 */
@Entity
@Table(name = "leave_ledger")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    @JsonIgnore
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_type_id")
    private LeaveType leaveType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransactionType transactionType;

    /** Positive for CREDIT; negative for DEBIT/VOID (stores the signed amount). */
    private double days;

    /** Balance after this transaction. */
    private double runningBalance;

    /** Reference — leave application ID or "ACCRUAL-YYYY-MM" etc. */
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    public enum TransactionType {
        CREDIT,      // accrual, carry-over, manual credit
        DEBIT,       // approved leave deducted
        VOID,        // cancellation reversal
        ADJUSTMENT   // admin manual correction
    }
}
