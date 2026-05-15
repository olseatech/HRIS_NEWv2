package com.ian.web.employee.leave;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import com.ian.web.employee.Employee;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

/**
 * Individual leave application filed by an employee.
 * Follows CSC Leave Application Form (CSC Form No. 6) — all sections A, B, C.
 *
 * Workflow states:
 *   PENDING     → employee submitted, waiting for HR processing
 *   ENDORSED    → HR admin has endorsed (supervisor action recorded)
 *   RETURNED    → returned to employee for correction
 *   APPROVED         → final approval by head/authorised approver
 *   CANCEL_REQUESTED → employee filed a cancellation request for an APPROVED leave
 *   DISAPPROVED      → disapproved
 *   CANCELLED        → cancelled by employee (PENDING only) or admin
 */
@Entity
@Table(name = "leave_application")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -----------------------------------------------------------------------
    // SECTION A — EMPLOYEE INFORMATION (via FK)
    // -----------------------------------------------------------------------

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    @NotNull
    @JsonIgnore
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_type_id")
    @NotNull
    private LeaveType leaveType;

    // -----------------------------------------------------------------------
    // SECTION B — DETAILS OF APPLICATION
    // -----------------------------------------------------------------------

    /** Inclusive start date. */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate dateFrom;

    /** Inclusive end date. */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate dateTo;

    /** Expected date of return to work (set at approval or by employee). */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedReturnDate;

    /** Working days computed by service layer (Mon–Fri, excluding holidays). */
    private double numberOfDays;

    /**
     * Leave sub-type specifics per CSC Form No. 6:
     *   Vacation Leave: "WITHIN_PHILIPPINES" | "ABROAD"
     *   Sick Leave:     "IN_HOSPITAL"        | "OUT_PATIENT"
     *   Others:         free text describing the specific special leave
     */
    @Column(length = 50)
    private String leaveSubType;

    /**
     * Detail for the leave sub-type (destination if abroad,
     * hospital/clinic name, specific reason for special leaves, etc.).
     */
    @Column(columnDefinition = "TEXT")
    private String leaveDetails;

    /** Employee's stated reason / purpose of leave. */
    @Column(columnDefinition = "TEXT")
    private String reason;

    /**
     * Set to {@code true} by the service layer when
     * {@code numberOfDays > leaveType.extendedLeaveDays}.
     * When {@code true} and the leave type requires endorsement for extended leaves,
     * the application must be endorsed before it can be approved.
     */
    @Column(name = "requires_higher_approval", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean requiresHigherApproval = false;

    /** Whether the employee requests money value / commutation of leave credits. */
    private boolean requestedCommutation;

    /** Server-side path to uploaded supporting document (medical cert, itinerary, etc.). */
    @Column(length = 500)
    private String attachmentPath;

    /** Original filename of the uploaded attachment (displayed in UI). */
    @Column(length = 255)
    private String attachmentFileName;

    /** MIME type of the uploaded attachment (used for safe Content-Type on download). */
    @Column(length = 100)
    private String attachmentMimeType;

    // -----------------------------------------------------------------------
    // SECTION C — DETAILS OF ACTION ON APPLICATION
    // Part 1: Certification of Leave Credits (HRMO)
    // -----------------------------------------------------------------------

    /** Days certified as available WITH pay. */
    private Double certDaysWithPay;

    /** Days certified WITHOUT pay (LWOP portion). */
    private Double certDaysWithoutPay;

    /** Employee ID of HRMO who certified the leave credits. */
    private Long hrmoId;

    /** Display name of HRMO certifier (denormalised for reports). */
    @Column(length = 255)
    private String hrmoName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hrmoCertDate;

    // Part 2: Recommendation by Immediate Supervisor / HR Admin Endorser
    // -----------------------------------------------------------------------

    /**
     * Supervisor recommendation (per CSC Form No. 6 Section C.2):
     *   "APPROVED_WITH_PAY" | "APPROVED_WITHOUT_PAY" | "DISAPPROVED"
     */
    @Column(length = 30)
    private String supervisorRecommendation;

    /** Remarks by the endorser during endorsement/return action. */
    @Column(columnDefinition = "TEXT")
    private String supervisorRemarks;

    /** Employee ID of the HR admin who endorsed this application. */
    private Long supervisorId;

    @Column(length = 255)
    private String supervisorName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate supervisorActionDate;

    // -----------------------------------------------------------------------
    // FORWARDING TRACKING — who this step was explicitly forwarded to
    // -----------------------------------------------------------------------

    /** Employee ID of the person this endorsement step was forwarded to. */
    private Long forwardedToId;

    /** Denormalised display name of the forwarded-to person (for reports and history). */
    @Column(length = 255)
    private String forwardedToName;

    /** Date the forwarding was recorded. */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate forwardedAt;

    // Part 3: Approved For (final decision by Head of Agency)
    // -----------------------------------------------------------------------

    /** Final number of days approved WITH pay. */
    private Double finalDaysWithPay;

    /** Final number of days approved WITHOUT pay (LWOP). */
    private Double finalDaysWithoutPay;

    /** Whether commutation of leave was approved. */
    private boolean commutationApproved;

    // -----------------------------------------------------------------------
    // WORKFLOW STATE
    // -----------------------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private LeaveStatus status = LeaveStatus.PENDING;

    /** Whether the final approved leave is with or without pay. */
    private boolean withPay = true;

    // -----------------------------------------------------------------------
    // FINAL APPROVER (Head of Agency / Authorised Signatory)
    // -----------------------------------------------------------------------

    private Long approvedById;

    @Column(length = 255)
    private String approvedByName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate approvedDate;

    /** Remarks from the final approver (disapproval reason, conditions, etc.). */
    @Column(columnDefinition = "TEXT")
    private String remarks;

    // -----------------------------------------------------------------------
    // SYSTEM / AUDIT
    // -----------------------------------------------------------------------

    /** Timestamp when the employee originally submitted the application. */
    private LocalDateTime appliedDateTime = LocalDateTime.now();

    /** Whether this application has already been reflected in the leave balance ledger. */
    private boolean ledgerPosted;

    // Cancellation tracking
    private Long   cancelledById;
    @Column(length = 255)
    private String cancelledByName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate cancelledDate;
    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    // Cancellation REQUEST tracking (2-step workflow for APPROVED leaves)
    /** Server-side path of the letter of cancellation upload. */
    @Column(length = 500)
    private String cancellationLetterPath;

    /** Original filename of the cancellation letter (shown in admin UI). */
    @Column(length = 255)
    private String cancellationLetterFileName;

    /** MIME type of the cancellation letter (for safe Content-Type on download). */
    @Column(length = 100)
    private String cancellationLetterMimeType;

    /** Employee ID of the person who submitted the cancellation request. */
    private Long cancellationRequestedById;

    /** Display name of the requester (denormalised for reports). */
    @Column(length = 255)
    private String cancellationRequestedByName;

    /** Date the cancellation was requested. */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate cancellationRequestedDate;

    // HR Acknowledgment tracking (Travel Leave 3-step cancellation workflow only)

    /** Employee ID of the HR admin who acknowledged the TL cancellation request. */
    private Long hrAcknowledgedById;

    /** Display name of the acknowledging HR admin (denormalised for reports). */
    @Column(length = 255)
    private String hrAcknowledgedByName;

    /** Date the HR acknowledgment was recorded. */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hrAcknowledgedDate;

    /** Optional remarks from the HR admin at acknowledgment time. */
    @Column(columnDefinition = "TEXT")
    private String hrAcknowledgmentRemarks;

    // -----------------------------------------------------------------------
    // TRANSIENT
    // -----------------------------------------------------------------------

    @Transient
    private String showMode;

    // -----------------------------------------------------------------------
    // STATUS ENUM
    // -----------------------------------------------------------------------

    public enum LeaveStatus {
        /** Submitted by employee — awaiting HR processing. */
        PENDING,
        /** HR admin has endorsed (forwarded to approving authority). */
        ENDORSED,
        /** HR has verified leave credits and document completeness (new hierarchical workflow). */
        HR_VERIFIED,
        /** Division Head (Division.approver1 or approver2) has endorsed (new hierarchical workflow). */
        ENDORSED_DIV,
        /** Secretary has endorsed — only required when requiresHigherApproval=true (new hierarchical workflow). */
        ENDORSED_SEC,
        /** Returned to employee for correction or additional documents. */
        RETURNED,
        /** Final approval granted by head of agency / authorised approver. */
        APPROVED,
        /**
         * Employee has submitted a letter requesting cancellation of an APPROVED leave.
         * Awaiting decision by the Head of Agency / Final Approver.
         */
        CANCEL_REQUESTED,
        /**
         * HR Admin has acknowledged the Travel Leave (TL) cancellation request.
         * Intermediate step for Travel Leave only — awaiting final approval.
         */
        CANCEL_HR_ACKNOWLEDGED,
        /** Disapproved by approving authority. */
        DISAPPROVED,
        /** Cancelled by employee (while PENDING) or admin. */
        CANCELLED
    }
}
