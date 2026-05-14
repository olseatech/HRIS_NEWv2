package com.ian.web.employee.leave;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seeds default Philippine Government leave types on first run.
 * Runs after application context is ready (Order 10 to give JPA time to create tables).
 *
 * Based on:
 *   - CSC MC No. 41 s.1998  (VL / SL accrual rules)
 *   - RA 11210 (Expanded Maternity Leave)
 *   - RA 8187 (Paternity Leave)
 *   - RA 8972 (Solo Parent Welfare Act)
 *   - RA 9710 (Magna Carta for Women — Special Leave Benefit)
 *   - CSC policies on Forced/Mandatory Leave and Study Leave
 */
@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class LeaveDataInitializer implements CommandLineRunner {

    private final LeaveTypeRepository leaveTypeRepo;

    @Override
    public void run(String... args) {
        try {
            runSeeding();
        } catch (Exception e) {
            log.warn("Leave type seeding skipped — tables may not exist yet. "
                   + "Run leave_migration.sql and restart. Error: {}", e.getMessage());
        }
    }

    private void runSeeding() {
        List<LeaveType> existing = leaveTypeRepo.findAllByOrderBySortOrderAscLeaveNameAsc();
        if (existing.isEmpty()) {
            log.info("Seeding default Philippine Government leave types...");
            seedAll();
            log.info("Default leave types seeded successfully.");
        } else {
            log.info("Leave types already seeded ({} found). Skipping seed.", existing.size());
        }

        // Always run policy patch to ensure extended-leave fields are set for
        // existing deployments that were seeded before this feature was added.
        patchExtendedLeavePolicy();
    }

    private void seedAll() {
        //                code   name                                 description
        //                maxDays accrual carryOver  commutable lwop  medCert  eligible      sort
        //                extLeave reqDoc  reqEndorse
        seed("VL",  "Vacation Leave",
                "CSC MC No. 41 s.1998. Earned at 1.25 days/month. Carry-over max 30 days.",
                0, 1.25, 30, true, false, false, "", 1,
                5, false, true);

        seed("SL",  "Sick Leave",
                "CSC MC No. 41 s.1998. Earned at 1.25 days/month. Carry-over max 30 days. Medical cert required for >5 days.",
                0, 1.25, 30, false, false, true, "", 2,
                5, true, true);

        seed("FL",  "Forced/Mandatory Leave",
                "5 working days mandatory per year charged against VL. All permanent employees.",
                5, 0, 0, false, false, false, "PLANTILLA", 3,
                0, false, true);

        seed("SPL", "Special Privilege Leave",
                "3 days per year for personal milestones, no accrual, non-cumulative.",
                3, 0, 0, false, false, false, "", 4,
                0, false, true);

        seed("ML",  "Maternity Leave",
                "RA 11210. 105 days with pay (120 days for solo parents). Female employees only.",
                120, 0, 0, false, false, true, "", 5,
                0, false, true);

        seed("PL",  "Paternity Leave",
                "RA 8187. 7 days for married male employees, per childbirth, max 4 deliveries.",
                7, 0, 0, false, false, false, "", 6,
                0, false, true);

        seed("SLB", "Special Leave Benefit (Gynecological)",
                "RA 9710 Magna Carta for Women. Max 2 months for gynaecological surgery. Female employees.",
                60, 0, 0, false, false, true, "", 7,
                0, false, true);

        seed("SoP", "Solo Parent Leave",
                "RA 8972. 7 days per year. Solo parent employees only.",
                7, 0, 0, false, false, false, "", 8,
                0, false, true);

        seed("SdL", "Study Leave",
                "CSC. Up to 6 months with pay for bar/board review or masters. With service obligation.",
                180, 0, 0, false, false, false, "PLANTILLA", 9,
                5, false, true);

        seed("RL",  "Rehabilitation Leave",
                "For employees injured in the line of duty. Duration per medical recommendation.",
                0, 0, 0, false, false, true, "", 10,
                0, false, true);

        seed("LWOP","Leave Without Pay",
                "When leave credits are exhausted. Affects service credit computation.",
                0, 0, 0, false, true, false, "", 11,
                0, false, true);
    }

    private void seed(String code, String name, String desc,
                      double maxDays, double accrual, double carryOver,
                      boolean commutable, boolean lwop, boolean medCert,
                      String eligible, int sortOrder,
                      int extLeave, boolean reqDoc, boolean reqEndorse) {
        if (leaveTypeRepo.existsByLeaveCode(code)) return;

        LeaveType lt = new LeaveType();
        lt.setLeaveCode(code);
        lt.setLeaveName(name);
        lt.setDescription(desc);
        lt.setMaxDaysPerYear(maxDays);
        lt.setAccrualPerMonth(accrual);
        lt.setCarryOverMax(carryOver);
        lt.setCommutable(commutable);
        lt.setLwopType(lwop);
        lt.setRequiresMedCert(medCert);
        lt.setEligibleEmploymentTypes(eligible);
        lt.setSortOrder(sortOrder);
        lt.setActive(true);
        lt.setExtendedLeaveDays(extLeave);
        lt.setRequiresDocForExtended(reqDoc);
        lt.setRequiresEndorsementForExtended(reqEndorse);
        leaveTypeRepo.save(lt);
    }

    /**
     * Patches extended-leave policy fields for existing deployments.
     * Idempotent: only updates rows where {@code extendedLeaveDays == 0}
     * and the code is one that should have a non-zero threshold.
     * Once an admin changes the threshold, this method will not overwrite it.
     */
    private void patchExtendedLeavePolicy() {
        patchType("VL",  5, false, true);
        patchType("SL",  5, true,  true);
        patchType("SdL", 5, false, true);
    }

    private void patchType(String code, int extLeave, boolean reqDoc, boolean reqEndorse) {
        leaveTypeRepo.findByLeaveCode(code).ifPresent(lt -> {
            if (lt.getExtendedLeaveDays() == 0) {
                lt.setExtendedLeaveDays(extLeave);
                lt.setRequiresDocForExtended(reqDoc);
                lt.setRequiresEndorsementForExtended(reqEndorse);
                leaveTypeRepo.save(lt);
                log.info("Patched extended leave policy for leave code '{}'.", code);
            }
        });
    }
}
