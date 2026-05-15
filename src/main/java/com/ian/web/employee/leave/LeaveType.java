package com.ian.web.employee.leave;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import lombok.*;

/**
 * Configurable leave type (Vacation Leave, Sick Leave, etc.).
 * Not hard-coded — HR Admin manages these via System Settings.
 */
@Entity
@Table(name = "leave_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short code used in reports: VL, SL, FL, SPL, ML, PL, SLBWOP, etc. */
    @NotBlank
    @Column(unique = true, length = 20)
    private String leaveCode;

    @NotBlank
    private String leaveName;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Maximum days that can be accrued per calendar year (0 = unlimited / LWOP). */
    private double maxDaysPerYear;

    /** Days earned per month of service (0 = not accrual-based, e.g. Maternity). */
    private double accrualPerMonth;

    /** Maximum unused balance allowed to carry over to next year. */
    private double carryOverMax;

    /** Whether the leave type is commutable to cash. */
    private boolean commutable;

    /** Whether this leave type counts as Leave Without Pay when balance is 0. */
    private boolean lwopType;

    /** Whether a medical certificate is required. */
    private boolean requiresMedCert;

    /**
     * Comma-separated employment type eligibility filter,
     * e.g. "PERMANENT,CASUAL" — empty means all types eligible.
     */
    @Column(length = 500)
    private String eligibleEmploymentTypes;

    /** Whether this leave type is currently active/selectable. */
    private boolean active = true;

    /** Display sort order in forms and reports. */
    private int sortOrder;

    /**
     * Month (1–12) on which annual balance resets to 0 (e.g. 1 = January for SPL).
     * Null means no annual reset.
     */
    private Integer annualResetMonth;

    /**
     * Days of return-of-service obligation for study leave (SdL).
     * 0 means no obligation.
     */
    private int serviceObligationDays;

    // -----------------------------------------------------------------------
    // EXTENDED LEAVE POLICY (CSC: >5 working days)
    // -----------------------------------------------------------------------

    /**
     * Extended leave threshold in working days.
     * 0 = disabled (no extended-leave policy for this type, e.g. ML, PL, SPL).
     * Default 5 per CSC policy for VL and SL.
     * Configurable per agency/LGU via the Leave Types admin page.
     * Boxed Integer to handle NULL values from existing DB records.
     */
    @Column(name = "extended_leave_days")
    private Integer extendedLeaveDays = 0;

    /**
     * If true, a supporting document upload is required when
     * {@code numberOfDays > extendedLeaveDays}.
     * CSC: required for Sick Leave exceeding 5 working days.
     * Boxed Boolean to handle NULL values from existing DB records.
     */
    @Column(name = "requires_doc_for_extended")
    private Boolean requiresDocForExtended = false;

    /**
     * If true, an endorsement step is mandatory before final approval
     * when {@code numberOfDays > extendedLeaveDays}.
     * Prevents direct PENDING → APPROVED for extended leaves.
     * Boxed Boolean to handle NULL values from existing DB records.
     */
    @Column(name = "requires_endorsement_for_extended")
    private Boolean requiresEndorsementForExtended = true;

    /**
     * If true, HR must verify leave credits/documents (HR_VERIFIED step) before Division Head
     * can endorse. Activates the new hierarchical endorsement workflow for this leave type.
     * Defaults to false so existing leave types keep the old simple workflow.
     */
    @Column(name = "requires_hr_verification")
    private boolean requiresHrVerification = false;

    /**
     * If true, a Division Head (Division.approver1 or approver2) must endorse before final approval.
     * When combined with requiresHrVerification, full chain: PENDING → HR_VERIFIED → ENDORSED_DIV
     * (→ ENDORSED_SEC if requiresHigherApproval) → APPROVED.
     */
    @Column(name = "requires_division_endorsement")
    private boolean requiresDivisionEndorsement = false;

    @Transient
    private String showMode;
}
