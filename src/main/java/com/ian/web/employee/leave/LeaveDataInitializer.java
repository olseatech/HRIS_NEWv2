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
        if (!existing.isEmpty()) {
            log.info("Leave types already seeded ({} found). Skipping.", existing.size());
            return;
        }

        log.info("Seeding default Philippine Government leave types...");

        seed("VL",  "Vacation Leave",
                "CSC MC No. 41 s.1998. Earned at 1.25 days/month. Carry-over max 30 days.",
                0, 1.25, 30, true, false, false, "", 1);

        seed("SL",  "Sick Leave",
                "CSC MC No. 41 s.1998. Earned at 1.25 days/month. Carry-over max 30 days. Medical cert required for >5 days.",
                0, 1.25, 30, false, false, true, "", 2);

        seed("FL",  "Forced/Mandatory Leave",
                "5 working days mandatory per year charged against VL. All permanent employees.",
                5, 0, 0, false, false, false, "PLANTILLA", 3);

        seed("SPL", "Special Privilege Leave",
                "3 days per year for personal milestones, no accrual, non-cumulative.",
                3, 0, 0, false, false, false, "", 4);

        seed("ML",  "Maternity Leave",
                "RA 11210. 105 days with pay (120 days for solo parents). Female employees only.",
                120, 0, 0, false, false, true, "", 5);

        seed("PL",  "Paternity Leave",
                "RA 8187. 7 days for married male employees, per childbirth, max 4 deliveries.",
                7, 0, 0, false, false, false, "", 6);

        seed("SLB", "Special Leave Benefit (Gynecological)",
                "RA 9710 Magna Carta for Women. Max 2 months for gynaecological surgery. Female employees.",
                60, 0, 0, false, false, true, "", 7);

        seed("SoP", "Solo Parent Leave",
                "RA 8972. 7 days per year. Solo parent employees only.",
                7, 0, 0, false, false, false, "", 8);

        seed("SdL", "Study Leave",
                "CSC. Up to 6 months with pay for bar/board review or masters. With service obligation.",
                180, 0, 0, false, false, false, "PLANTILLA", 9);

        seed("RL",  "Rehabilitation Leave",
                "For employees injured in the line of duty. Duration per medical recommendation.",
                0, 0, 0, false, false, true, "", 10);

        seed("LWOP","Leave Without Pay",
                "When leave credits are exhausted. Affects service credit computation.",
                0, 0, 0, false, true, false, "", 11);

        log.info("Default leave types seeded successfully.");
    }

    private void seed(String code, String name, String desc,
                      double maxDays, double accrual, double carryOver,
                      boolean commutable, boolean lwop, boolean medCert,
                      String eligible, int sortOrder) {
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
        leaveTypeRepo.save(lt);
    }
}
