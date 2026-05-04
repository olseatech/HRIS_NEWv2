package com.ian.web.employee.leave;

import javax.persistence.*;
import com.ian.web.employee.Employee;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

/**
 * Running leave balance per employee per leave type per calendar year.
 * Recomputed by the service layer after each ledger transaction.
 */
@Entity
@Table(name = "leave_balance",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "leave_type_id", "balance_year"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveBalance {

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

    @Column(name = "balance_year")
    private int balanceYear;

    /** Total days earned / credited this year (accrual + carry-over). */
    private double totalEarned;

    /** Total days consumed (approved leave deductions) this year. */
    private double totalUsed;

    /** Current available balance = totalEarned - totalUsed. */
    private double balance;
}
