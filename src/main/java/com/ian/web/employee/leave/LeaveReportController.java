package com.ian.web.employee.leave;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Leave reports controller.
 *
 * Routes:
 *   GET  /leave-report                      — leave summary report page (admin)
 *   GET  /leave-card/{employeeId}/{year}     — individual leave card view (admin)
 *   GET  /leave-export-csv                  — download CSV of all applications for a year
 *   GET  /leave-balance-export-csv/{year}   — download CSV of all employee balances for a year
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LeaveReportController {

    private final LeaveApplicationRepository applicationRepo;
    private final LeaveBalanceRepository     balanceRepo;
    private final LeaveTypeRepository        leaveTypeRepo;
    private final EmployeeRepository         employeeRepository;
    private final LeaveService               leaveService;

    // -----------------------------------------------------------------------
    // LEAVE SUMMARY REPORT PAGE
    // -----------------------------------------------------------------------

    @GetMapping("/leave-report")
    public String reportPage(
            @RequestParam(value = "year",       required = false) Integer year,
            @RequestParam(value = "employeeId", required = false) Long    employeeId,
            @RequestParam(value = "leaveTypeId",required = false) Long    leaveTypeId,
            @RequestParam(value = "status",     required = false) String  status,
            Model model, HttpServletRequest request) {

        if (!isAdmin(request)) return "redirect:/dashboard";

        int selectedYear = (year != null) ? year : LocalDate.now().getYear();
        try {
            // Default: all applications for the selected year (pending, endorsed, approved, etc.)
            List<LeaveApplication> applications;
            if (employeeId != null && leaveTypeId != null) {
                applications = applicationRepo.findByEmployeeAndYear(employeeId, selectedYear)
                        .stream()
                        .filter(a -> a.getLeaveType() != null && a.getLeaveType().getId() != null
                                && a.getLeaveType().getId().equals(leaveTypeId))
                        .collect(java.util.stream.Collectors.toList());
            } else if (employeeId != null) {
                applications = applicationRepo.findByEmployeeAndYear(employeeId, selectedYear);
            } else if (leaveTypeId != null) {
                final Long ltId = leaveTypeId;
                applications = applicationRepo.findAllByOrderByAppliedDateTimeDesc()
                        .stream()
                        .filter(a -> a.getDateFrom() != null
                                && a.getDateFrom().getYear() == selectedYear
                                && a.getLeaveType() != null
                                && a.getLeaveType().getId() != null
                                && a.getLeaveType().getId().equals(ltId))
                        .collect(java.util.stream.Collectors.toList());
            } else {
                applications = applicationRepo.findAllByOrderByAppliedDateTimeDesc()
                        .stream()
                        .filter(a -> a.getDateFrom() != null
                                && a.getDateFrom().getYear() == selectedYear)
                        .collect(java.util.stream.Collectors.toList());
            }

            // Apply status filter if provided
            if (status != null && !status.isBlank()) {
                try {
                    LeaveApplication.LeaveStatus st = LeaveApplication.LeaveStatus.valueOf(status);
                    applications = applications.stream()
                            .filter(a -> a.getStatus() == st)
                            .collect(java.util.stream.Collectors.toList());
                } catch (IllegalArgumentException ignored) { }
            }

            model.addAttribute("applications", applications);
            model.addAttribute("employees",    employeeRepository.findAll());
            model.addAttribute("leaveTypes",   leaveTypeRepo.findByActiveTrueOrderBySortOrderAscLeaveNameAsc());
        } catch (Exception e) {
            log.error("Leave report page error", e);
            model.addAttribute("applications", java.util.Collections.emptyList());
            model.addAttribute("employees",    java.util.Collections.emptyList());
            model.addAttribute("leaveTypes",   java.util.Collections.emptyList());
            model.addAttribute("uxmessage",
                new UXMessage("ERROR",
                    "DB error: " + e.getMessage()
                        + " — Run leave_migration.sql then restart."));
        }

        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("selectedLeaveTypeId", leaveTypeId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentYear",  LocalDate.now().getYear());
        model.addAttribute("statuses",     LeaveApplication.LeaveStatus.values());
        return "employee/leave/leave-report";
    }

    // -----------------------------------------------------------------------
    // INDIVIDUAL LEAVE CARD (per employee per year)
    // -----------------------------------------------------------------------

    @GetMapping("/leave-card/{employeeId}/{year}")
    public String leaveCard(
            @PathVariable Long employeeId,
            @PathVariable int  year,
            Model model, HttpServletRequest request) {

        if (!isAdmin(request)) return "redirect:/dashboard";

        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) return "redirect:/leave-report";

        model.addAttribute("employee",     employee);
        model.addAttribute("year",         year);
        model.addAttribute("balances",     leaveService.getBalancesForEmployee(employeeId, year));
        model.addAttribute("ledger",       leaveService.getLedgerForEmployee(employeeId));
        model.addAttribute("applications", applicationRepo.findByEmployeeAndYear(employeeId, year));
        model.addAttribute("leaveTypes",   leaveTypeRepo.findByActiveTrueOrderBySortOrderAscLeaveNameAsc());
        return "employee/leave/leave-card";
    }

    // -----------------------------------------------------------------------
    // CSV EXPORT — ALL APPLICATIONS FOR A YEAR
    // -----------------------------------------------------------------------

    @GetMapping("/leave-export-csv")
        public void exportApplicationsCsv(
            @RequestParam(value = "year", required = false) Integer year,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (!isAdmin(request)) { response.setStatus(403); return; }

        int selectedYear = (year != null) ? year : LocalDate.now().getYear();
        List<LeaveApplication> applications = applicationRepo.findAllByOrderByAppliedDateTimeDesc()
                .stream()
                .filter(a -> a.getDateFrom() != null && a.getDateFrom().getYear() == selectedYear)
                .collect(java.util.stream.Collectors.toList());

        StringBuilder csv = new StringBuilder(1024);
        csv.append("Application No,Employee Name,Employee No,Division,")
           .append("Leave Type,Date From,Date To,Working Days,")
           .append("Leave Sub-Type,Leave Details,Reason,")
           .append("Status,With Pay,Commutation Requested,")
           .append("HRMO Cert Date,HRMO Name,Cert Days With Pay,Cert Days Without Pay,")
           .append("Supervisor Recommendation,Supervisor Name,Supervisor Date,Supervisor Remarks,")
           .append("Final Days With Pay,Final Days Without Pay,")
           .append("Approved By,Approved Date,Remarks,")
           .append("Filed On,Expected Return Date\n");

        for (LeaveApplication a : applications) {
            Employee employee = a.getEmployee();
            LeaveType leaveType = a.getLeaveType();
            String division = "";
            if (employee != null && employee.getDivision() != null) {
            division = employee.getDivision().getDivisionName();
            }

            csv.append(csvField(a.getId())).append(',')
               .append(csvField(employee != null ? employee.getDisplayName() : "")).append(',')
               .append(csvField(employee != null ? employee.getEmpNo() : "")).append(',')
               .append(csvField(division)).append(',')
               .append(csvField(leaveType != null ? leaveType.getLeaveName() : "")).append(',')
               .append(csvField(a.getDateFrom())).append(',')
               .append(csvField(a.getDateTo())).append(',')
               .append(csvField(a.getNumberOfDays())).append(',')
               .append(csvField(a.getLeaveSubType())).append(',')
               .append(csvField(a.getLeaveDetails())).append(',')
               .append(csvField(a.getReason())).append(',')
               .append(csvField(a.getStatus())).append(',')
               .append(csvField(a.isWithPay() ? "Yes" : "No")).append(',')
               .append(csvField(a.isRequestedCommutation() ? "Yes" : "No")).append(',')
               .append(csvField(a.getHrmoCertDate())).append(',')
               .append(csvField(a.getHrmoName())).append(',')
               .append(csvField(a.getCertDaysWithPay())).append(',')
               .append(csvField(a.getCertDaysWithoutPay())).append(',')
               .append(csvField(a.getSupervisorRecommendation())).append(',')
               .append(csvField(a.getSupervisorName())).append(',')
               .append(csvField(a.getSupervisorActionDate())).append(',')
               .append(csvField(a.getSupervisorRemarks())).append(',')
               .append(csvField(a.getFinalDaysWithPay())).append(',')
               .append(csvField(a.getFinalDaysWithoutPay())).append(',')
               .append(csvField(a.getApprovedByName())).append(',')
               .append(csvField(a.getApprovedDate())).append(',')
               .append(csvField(a.getRemarks())).append(',')
               .append(csvField(a.getAppliedDateTime() != null
                   ? a.getAppliedDateTime().toLocalDate() : null)).append(',')
               .append(csvField(a.getExpectedReturnDate())).append('\n');
        }

        writeCsvResponse(response,
            "leave-applications-" + selectedYear + ".csv",
            csv.toString());
    }

    // -----------------------------------------------------------------------
    // CSV EXPORT — LEAVE BALANCES FOR ALL EMPLOYEES
    // -----------------------------------------------------------------------

    @GetMapping("/leave-balance-export-csv/{year}")
        public void exportBalancesCsv(
            @PathVariable int year,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (!isAdmin(request)) { response.setStatus(403); return; }

        List<Employee> employees = employeeRepository.findAll();
        StringBuilder csv = new StringBuilder(1024);
        csv.append("Employee Name,Employee No,Division,Leave Type,")
           .append("Year,Total Earned,Total Used,Balance\n");

        for (Employee emp : employees) {
            List<LeaveBalance> balances;
            try {
                balances = leaveService.getBalancesForEmployee(emp.getId(), year);
            } catch (Exception e) {
                log.warn("Failed to load balances for employee {} in year {}: {}",
                        emp.getId(), year, e.getMessage());
                continue;
            }

            String division = emp.getDivision() != null
                    ? emp.getDivision().getDivisionName() : "";
            for (LeaveBalance b : balances) {
                LeaveType leaveType = b.getLeaveType();
                csv.append(csvField(emp.getDisplayName())).append(',')
                   .append(csvField(emp.getEmpNo())).append(',')
                   .append(csvField(division)).append(',')
                   .append(csvField(leaveType != null ? leaveType.getLeaveName() : "")).append(',')
                   .append(csvField(b.getBalanceYear())).append(',')
                   .append(csvField(b.getTotalEarned())).append(',')
                   .append(csvField(b.getTotalUsed())).append(',')
                   .append(csvField(b.getBalance())).append('\n');
            }
        }

        writeCsvResponse(response,
                "leave-balances-" + year + ".csv",
                csv.toString());
    }

    // -----------------------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------------------

    private Employee getActor(HttpServletRequest request) {
        Employee actor = (Employee) request.getSession().getAttribute("actorObj");
        if (actor == null) {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Employee) {
                actor = (Employee) auth.getPrincipal();
            }
        }
        return actor;
    }

    private boolean isAdmin(HttpServletRequest request) {
        Employee actor = getActor(request);
        return actor != null && "ROLE_ADMIN".equals(actor.getUserType());
    }

    private String csvField(Object value) {
        if (value == null) return "";
        String s = value.toString().replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }

    private void writeCsvResponse(HttpServletResponse response, String filename, String csvBody) {
        try {
            byte[] bytes = csvBody.getBytes(StandardCharsets.UTF_8);
            response.resetBuffer();
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + filename + "\"");
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.flushBuffer();
        } catch (Exception e) {
            log.error("Failed to write CSV response: {}", e.getMessage(), e);
        }
    }
}
