package com.ian.web.employee.leave;

import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ian.web.employee.Employee;
import lombok.RequiredArgsConstructor;

/**
 * REST API endpoints for leave data — consumed by DataTables AJAX calls
 * and any future front-end widgets.
 *
 * All responses return JSON.  Routes match existing /api/** pattern.
 *
 * GET  /api/leave/all                        — all applications (admin)
 * GET  /api/leave/pending                    — pending only (admin)
 * GET  /api/leave/by-employee/{id}           — employee's own applications
 * GET  /api/leave/balance/{employeeId}/{year} — balances for employee+year
 * GET  /api/leave/ledger/{employeeId}        — ledger for employee
 */
@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveApiController {

    private final LeaveApplicationRepository applicationRepo;
    private final LeaveBalanceRepository     balanceRepo;
    private final LeaveLedgerRepository      ledgerRepo;
    private final LeaveService               leaveService;

    /** All applications — admin only. */
    @GetMapping("/all")
    public ResponseEntity<List<LeaveApplication>> getAll(HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(applicationRepo.findAllByOrderByAppliedDateTimeDesc());
    }

    /** Pending applications — admin only. Returns flat DTOs; employee is @JsonIgnore on entity so projected here. */
    @GetMapping("/pending")
    public ResponseEntity<List<java.util.Map<String,Object>>> getPending(HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).build();
        List<LeaveApplication> apps = applicationRepo.findAllPendingFetched();
        List<java.util.Map<String,Object>> result = new java.util.ArrayList<>();
        for (LeaveApplication a : apps) {
            java.util.Map<String,Object> dto = new java.util.LinkedHashMap<>();
            dto.put("id",              a.getId());
            dto.put("dateFrom",        a.getDateFrom());
            dto.put("dateTo",          a.getDateTo());
            dto.put("numberOfDays",    a.getNumberOfDays());
            dto.put("appliedDateTime", a.getAppliedDateTime());
            dto.put("leaveName",       a.getLeaveType() != null ? a.getLeaveType().getLeaveName() : "");
            Employee emp = a.getEmployee();
            dto.put("employeeName",     emp != null ? emp.getDisplayName() : "");
            dto.put("employeePosition", emp != null && emp.getPositionTitle() != null
                                          ? emp.getPositionTitle().getPositionTitleName() : "");
            dto.put("employeeDivision", emp != null && emp.getDivision() != null
                                          ? emp.getDivision().getDivisionName() : "");
            result.add(dto);
        }
        return ResponseEntity.ok(result);
    }

    /** Applications for a specific employee (admin or own employee). */
    @GetMapping("/by-employee/{employeeId}")
    public ResponseEntity<List<LeaveApplication>> byEmployee(
            @PathVariable Long employeeId, HttpServletRequest request) {
        if (!isAdminOrSelf(request, employeeId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(
                applicationRepo.findByEmployeeIdOrderByAppliedDateTimeDesc(employeeId));
    }

    /** Leave balances for a specific employee and year. */
    @GetMapping("/balance/{employeeId}/{year}")
    public ResponseEntity<List<LeaveBalance>> getBalance(
            @PathVariable Long employeeId,
            @PathVariable int year,
            HttpServletRequest request) {
        if (!isAdminOrSelf(request, employeeId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(leaveService.getBalancesForEmployee(employeeId, year));
    }

    /** Balance for current year — shorthand. */
    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<List<LeaveBalance>> getBalanceCurrent(
            @PathVariable Long employeeId, HttpServletRequest request) {
        if (!isAdminOrSelf(request, employeeId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(
                leaveService.getBalancesForEmployee(employeeId, LocalDate.now().getYear()));
    }

    /** Full ledger for a specific employee. */
    @GetMapping("/ledger/{employeeId}")
    public ResponseEntity<List<LeaveLedger>> getLedger(
            @PathVariable Long employeeId, HttpServletRequest request) {
        if (!isAdminOrSelf(request, employeeId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(leaveService.getLedgerForEmployee(employeeId));
    }

    /** Process approval (PATCH-style POST for form-compatibility). */
    @PostMapping("/approve/{id}")
    public ResponseEntity<LeaveApplication> approve(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).build();
        Employee actor = actor(request);
        try {
            return ResponseEntity.ok(
                    leaveService.approveLeave(id, actor.getId(), actor.getDisplayName(), remarks));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** Process disapproval. */
    @PostMapping("/disapprove/{id}")
    public ResponseEntity<LeaveApplication> disapprove(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(403).build();
        Employee actor = actor(request);
        try {
            return ResponseEntity.ok(
                    leaveService.disapproveLeave(id, actor.getId(), actor.getDisplayName(), remarks));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Employee actor(HttpServletRequest request) {
        return (Employee) request.getSession().getAttribute("actorObj");
    }

    private boolean isAdmin(HttpServletRequest request) {
        Employee actor = actor(request);
        return actor != null && "ROLE_ADMIN".equals(actor.getUserType());
    }

    private boolean isAdminOrSelf(HttpServletRequest request, Long employeeId) {
        Employee actor = actor(request);
        if (actor == null) return false;
        return "ROLE_ADMIN".equals(actor.getUserType()) || actor.getId() == employeeId;
    }
}
