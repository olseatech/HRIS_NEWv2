package com.ian.web.employee.leave;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.*;

/**
 * Immutable audit trail for every status change on a LeaveApplication.
 * One record is written each time the workflow state transitions.
 *
 * Examples:
 *   PENDING      → ENDORSED    (HR admin endorses)
 *   ENDORSED     → RETURNED    (HR admin returns for correction)
 *   RETURNED     → PENDING     (employee re-submits)
 *   ENDORSED     → APPROVED    (head approves)
 *   PENDING      → DISAPPROVED (HR admin disapproves directly)
 *   APPROVED     → CANCELLED   (admin cancels after approval)
 */
@Entity
@Table(name = "leave_status_history",
       indexes = @Index(name = "idx_lsh_application", columnList = "application_id"))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LeaveApplication application;

    /** Status BEFORE this transition (null for the initial PENDING submission). */
    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private LeaveApplication.LeaveStatus fromStatus;

    /** Status AFTER this transition. */
    @Enumerated(EnumType.STRING)
    @Column(length = 25, nullable = false)
    private LeaveApplication.LeaveStatus toStatus;

    /** Employee ID of the actor who triggered this transition. */
    private Long actorId;

    /** Display name of the actor (denormalised for reports). */
    @Column(length = 255)
    private String actorName;

    /** Role/capacity in which the actor acted (e.g. "HR Admin", "Supervisor", "Employee"). */
    @Column(length = 50)
    private String actorRole;

    /** Optional remarks provided at the time of the action. */
    @Column(columnDefinition = "TEXT")
    private String remarks;

    /** Timestamp of this status change. */
    @Column(nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();
}
