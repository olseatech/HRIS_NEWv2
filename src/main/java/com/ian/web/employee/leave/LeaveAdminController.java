package com.ian.web.employee.leave;

import java.time.LocalDate;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.systemsettings.division.Division;
import com.ian.web.systemsettings.division.DivisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HR Admin controller for the Leave Management module.
 *
 * Routes:
 *   GET  /leave-management                         — full list (all statuses)
 *   GET  /leave-management/pending                 — pending only
 *   GET  /leave-management/actionable              — pending + endorsed
 *   GET  /leave-history/{id}                       — status history for one application
 *   POST /leave-endorse/{id}                       — endorse (PENDING → ENDORSED)
 *   POST /leave-return/{id}                        — return for correction
 *   POST /leave-approve/{id}                       — approve (PENDING|ENDORSED → APPROVED)
 *   POST /leave-disapprove/{id}                    — disapprove
 *   POST /leave-cancel-admin/{id}                  — admin cancels
 *   POST /leave-accrue-all                         — backfill accrual for all employees
 *   POST /leave-balance-adjust                     — manual balance adjustment
 *   GET  /employee-leave/{empId}/{showMode}/{hash} — employee leave profile
 *   POST /addLeaveAdmin                            — admin files leave on behalf of employee
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LeaveAdminController {

    private final LeaveApplicationRepository   applicationRepo;
    private final LeaveService                 leaveService;
    private final EmployeeRepository           employeeRepository;
    private final DivisionRepository           divisionRepository;
    private final LeaveTypeRepository          leaveTypeRepo;
    private final LeaveBalanceRepository       balanceRepo;
    private final LeaveStatusHistoryRepository historyRepo;
    private final FileStorageService           fileStorageService;

    // -----------------------------------------------------------------------
    // LIST VIEWS
    // -----------------------------------------------------------------------

    private static void noCache(HttpServletResponse res) {
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);
    }

    @GetMapping("/leave-management")
    public String listAll(Model model, HttpServletRequest request,
                          HttpServletResponse response) {
        noCache(response);
        if (!isAdmin(request)) return "redirect:/dashboard";

        Employee actor = getActor(request);
        try {
            long pendingCount         = applicationRepo.countByStatus(LeaveApplication.LeaveStatus.PENDING);
            long endorsedCount        = applicationRepo.countByStatus(LeaveApplication.LeaveStatus.ENDORSED);
            long cancelRequestedCount = applicationRepo.countByStatus(LeaveApplication.LeaveStatus.CANCEL_REQUESTED)
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.CANCEL_HR_ACKNOWLEDGED);
            long actionableCount      = pendingCount + endorsedCount
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.HR_VERIFIED)
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.ENDORSED_DIV)
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.ENDORSED_SEC);
            List<Employee> allEmployees = employeeRepository.findAllWithAssociationsFetched();
            model.addAttribute("applications",         applicationRepo.findAllFetched());
            model.addAttribute("pendingCount",         pendingCount);
            model.addAttribute("endorsedCount",        endorsedCount);
            model.addAttribute("actionableCount",      actionableCount);
            model.addAttribute("cancelRequestedCount", cancelRequestedCount);
            model.addAttribute("leaveTypes",           leaveTypeRepo.findByActiveTrueOrderBySortOrderAscLeaveNameAsc());
            model.addAttribute("employees",            allEmployees);
            model.addAttribute("currentYear",          LocalDate.now().getYear());
            model.addAttribute("actorUserType",        actor != null ? actor.getUserType() : "");
            model.addAttribute("actorId",              actor != null ? actor.getId() : null);
            addEndorsementModelAttributes(model, allEmployees);
        } catch (Exception e) {
            log.error("Leave management page error", e);
            model.addAttribute("applications",         java.util.Collections.emptyList());
            model.addAttribute("pendingCount",         0L);
            model.addAttribute("endorsedCount",        0L);
            model.addAttribute("actionableCount",      0L);
            model.addAttribute("cancelRequestedCount", 0L);
            model.addAttribute("leaveTypes",           java.util.Collections.emptyList());
            model.addAttribute("employees",            java.util.Collections.emptyList());
            model.addAttribute("divisionApprovers",    java.util.Collections.emptyList());
            model.addAttribute("secretaries",          java.util.Collections.emptyList());
            model.addAttribute("viceMayors",           java.util.Collections.emptyList());
            model.addAttribute("currentYear",          LocalDate.now().getYear());
            model.addAttribute("actorUserType",        "");
            model.addAttribute("actorId",              null);
            model.addAttribute("uxmessage",
                new UXMessage("ERROR", "DB error: " + e.getMessage()
                    + " — Run leave_migration.sql then restart."));
        }
        return "employee/leave/leave-management";
    }

    @GetMapping("/leave-management/pending")
    public String listPending(Model model, HttpServletRequest request,
                              HttpServletResponse response) {
        noCache(response);
        if (!isAdmin(request)) return "redirect:/dashboard";
        Employee actor = getActor(request);
        try {
            long pendingCount         = applicationRepo.countByStatus(LeaveApplication.LeaveStatus.PENDING);
            long endorsedCount        = applicationRepo.countByStatus(LeaveApplication.LeaveStatus.ENDORSED);
            long cancelRequestedCount = applicationRepo.countByStatus(LeaveApplication.LeaveStatus.CANCEL_REQUESTED)
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.CANCEL_HR_ACKNOWLEDGED);
            long actionableCount      = pendingCount + endorsedCount
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.HR_VERIFIED)
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.ENDORSED_DIV)
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.ENDORSED_SEC);
            List<Employee> allEmployees = employeeRepository.findAllWithAssociationsFetched();
            model.addAttribute("applications",         applicationRepo.findAllPendingFetched());
            model.addAttribute("filterLabel",          "Pending Only");
            model.addAttribute("pendingCount",         pendingCount);
            model.addAttribute("endorsedCount",        endorsedCount);
            model.addAttribute("actionableCount",      actionableCount);
            model.addAttribute("cancelRequestedCount", cancelRequestedCount);
            model.addAttribute("leaveTypes",           leaveTypeRepo.findByActiveTrueOrderBySortOrderAscLeaveNameAsc());
            model.addAttribute("employees",            allEmployees);
            model.addAttribute("currentYear",          LocalDate.now().getYear());
            model.addAttribute("actorUserType",        actor != null ? actor.getUserType() : "");
            model.addAttribute("actorId",              actor != null ? actor.getId() : null);
            addEndorsementModelAttributes(model, allEmployees);
        } catch (Exception e) {
            log.error("Leave management pending page error", e);
            model.addAttribute("applications",         java.util.Collections.emptyList());
            model.addAttribute("filterLabel",          "Pending Only");
            model.addAttribute("pendingCount",         0L);
            model.addAttribute("endorsedCount",        0L);
            model.addAttribute("actionableCount",      0L);
            model.addAttribute("cancelRequestedCount", 0L);
            model.addAttribute("leaveTypes",           java.util.Collections.emptyList());
            model.addAttribute("employees",            java.util.Collections.emptyList());
            model.addAttribute("divisionApprovers",    java.util.Collections.emptyList());
            model.addAttribute("secretaries",          java.util.Collections.emptyList());
            model.addAttribute("viceMayors",           java.util.Collections.emptyList());
            model.addAttribute("currentYear",          LocalDate.now().getYear());
            model.addAttribute("actorUserType",        "");
            model.addAttribute("actorId",              null);
            model.addAttribute("uxmessage",
                new UXMessage("ERROR", "DB error: " + e.getMessage()
                    + " — Run leave_migration.sql then restart."));
        }
        return "employee/leave/leave-management";
    }

    @GetMapping("/leave-management/actionable")
    public String listActionable(Model model, HttpServletRequest request,
                                 HttpServletResponse response) {
        noCache(response);
        if (!isAdmin(request)) return "redirect:/dashboard";
        Employee actor = getActor(request);
        try {
            long pendingCount         = applicationRepo.countByStatus(LeaveApplication.LeaveStatus.PENDING);
            long endorsedCount        = applicationRepo.countByStatus(LeaveApplication.LeaveStatus.ENDORSED);
            long cancelRequestedCount = applicationRepo.countByStatus(LeaveApplication.LeaveStatus.CANCEL_REQUESTED)
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.CANCEL_HR_ACKNOWLEDGED);
            long actionableCount      = pendingCount + endorsedCount
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.HR_VERIFIED)
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.ENDORSED_DIV)
                    + applicationRepo.countByStatus(LeaveApplication.LeaveStatus.ENDORSED_SEC);
            List<Employee> allEmployees = employeeRepository.findAllWithAssociationsFetched();
            model.addAttribute("applications",         applicationRepo.findAllActionableFetched(
                        java.util.Arrays.asList(LeaveApplication.LeaveStatus.PENDING,
                                                LeaveApplication.LeaveStatus.ENDORSED,
                                                LeaveApplication.LeaveStatus.HR_VERIFIED,
                                                LeaveApplication.LeaveStatus.ENDORSED_DIV,
                                                LeaveApplication.LeaveStatus.ENDORSED_SEC)));
            model.addAttribute("filterLabel",          "Pending & Endorsed");
            model.addAttribute("pendingCount",         pendingCount);
            model.addAttribute("endorsedCount",        endorsedCount);
            model.addAttribute("actionableCount",      actionableCount);
            model.addAttribute("cancelRequestedCount", cancelRequestedCount);
            model.addAttribute("leaveTypes",           leaveTypeRepo.findByActiveTrueOrderBySortOrderAscLeaveNameAsc());
            model.addAttribute("employees",            allEmployees);
            model.addAttribute("currentYear",          LocalDate.now().getYear());
            model.addAttribute("actorUserType",        actor != null ? actor.getUserType() : "");
            model.addAttribute("actorId",              actor != null ? actor.getId() : null);
            addEndorsementModelAttributes(model, allEmployees);
        } catch (Exception e) {
            log.error("Leave management actionable page error", e);
            model.addAttribute("applications",         java.util.Collections.emptyList());
            model.addAttribute("filterLabel",          "Pending & Endorsed");
            model.addAttribute("pendingCount",         0L);
            model.addAttribute("endorsedCount",        0L);
            model.addAttribute("actionableCount",      0L);
            model.addAttribute("cancelRequestedCount", 0L);
            model.addAttribute("leaveTypes",           java.util.Collections.emptyList());
            model.addAttribute("employees",            java.util.Collections.emptyList());
            model.addAttribute("divisionApprovers",    java.util.Collections.emptyList());
            model.addAttribute("secretaries",          java.util.Collections.emptyList());
            model.addAttribute("viceMayors",           java.util.Collections.emptyList());
            model.addAttribute("currentYear",          LocalDate.now().getYear());
            model.addAttribute("actorUserType",        "");
            model.addAttribute("actorId",              null);
            model.addAttribute("uxmessage",
                new UXMessage("ERROR", "DB error: " + e.getMessage()
                    + " — Run leave_migration.sql then restart."));
        }
        return "employee/leave/leave-management";
    }

    // -----------------------------------------------------------------------
    // EMPLOYEE LEAVE PROFILE
    // -----------------------------------------------------------------------

    @GetMapping("/employee-leave/{employeeId}/{showMode}/{empHashCode}")
    public String viewEmployeeLeave(
            @PathVariable long employeeId,
            @PathVariable String showMode,
            @PathVariable String empHashCode,
            Model model, HttpServletRequest request,
            HttpServletResponse response) {

        noCache(response);
        if (!isAdmin(request)) return "redirect:/dashboard";

        // Use findByIdAndEmpHashCodeFetched to prevent N+1 queries and lazy initialization
        Employee employee = employeeRepository
                .findByIdAndEmpHashCodeFetched(employeeId, empHashCode).orElse(null);

        int year = LocalDate.now().getYear();

        if (employee == null) {
            model.addAttribute("employee",     null);
            model.addAttribute("balances",     java.util.Collections.emptyList());
            model.addAttribute("ledger",       java.util.Collections.emptyList());
            model.addAttribute("applications", java.util.Collections.emptyList());
            model.addAttribute("leaveTypes",   java.util.Collections.emptyList());
            model.addAttribute("currentYear",  year);
            model.addAttribute("uxmessage",    new UXMessage("ERROR", "Employee not found."));
            return "employee/leave/employee-leave";
        }

        employee.setShowMode(showMode);

        try {
            model.addAttribute("balances", leaveService.getBalancesForEmployee(employeeId, year));
        } catch (Exception e) {
            log.warn("Failed to load balances for employee {}: {}", employeeId, e.getMessage());
            model.addAttribute("balances", java.util.Collections.emptyList());
        }
        try {
            model.addAttribute("ledger", leaveService.getLedgerForEmployeeFetched(employeeId));
        } catch (Exception e) {
            log.warn("Failed to load ledger for employee {}: {}", employeeId, e.getMessage());
            model.addAttribute("ledger", java.util.Collections.emptyList());
        }
        try {
            model.addAttribute("applications",
                    applicationRepo.findByEmployeeIdFetched(employeeId));
        } catch (Exception e) {
            log.warn("Failed to load applications for employee {}: {}", employeeId, e.getMessage());
            model.addAttribute("applications", java.util.Collections.emptyList());
        }

        model.addAttribute("employee",      employee);
        model.addAttribute("leaveTypes",    leaveTypeRepo.findByActiveTrueOrderBySortOrderAscLeaveNameAsc());
        model.addAttribute("currentYear",   year);
        model.addAttribute("newApplication", buildBlankApplication(employee));
        return "employee/leave/employee-leave";
    }

    // -----------------------------------------------------------------------
    // ENDORSE (PENDING → ENDORSED)
    // -----------------------------------------------------------------------

    @PostMapping("/leave-endorse/{id}")
    public String endorse(
            @PathVariable Long id,
            @RequestParam(required = false) String  recommendation,
            @RequestParam(required = false) String  remarks,
            @RequestParam(required = false) Double  certDaysWithPay,
            @RequestParam(required = false) Double  certDaysWithoutPay,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = getActor(request);
        if (!isAdmin(request)) return "redirect:/dashboard";
        try {
            leaveService.endorseLeave(id, actor.getId(), actor.getDisplayName(),
                    recommendation, remarks, certDaysWithPay, certDaysWithoutPay);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS", "Leave application #" + id + " endorsed successfully."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // RETURN FOR CORRECTION (PENDING|ENDORSED → RETURNED)
    // -----------------------------------------------------------------------

    @PostMapping("/leave-return/{id}")
    public String returnForCorrection(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = getActor(request);
        if (!isAdmin(request)) return "redirect:/dashboard";
        try {
            leaveService.returnForCorrection(id, actor.getId(), actor.getDisplayName(), remarks);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS",
                            "Leave application #" + id + " returned for correction."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // APPROVE (PENDING|ENDORSED → APPROVED)
    // -----------------------------------------------------------------------

    @PostMapping("/leave-approve/{id}")
    public String approve(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = getActor(request);
        if (!isAdmin(request)) return "redirect:/dashboard";
        try {
            leaveService.approveLeave(id, actor.getId(), actor.getDisplayName(), remarks);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS", "Leave application #" + id + " approved."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // DISAPPROVE
    // -----------------------------------------------------------------------

    @PostMapping("/leave-disapprove/{id}")
    public String disapprove(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = getActor(request);
        if (!isAdmin(request)) return "redirect:/dashboard";
        try {
            leaveService.disapproveLeave(id, actor.getId(), actor.getDisplayName(), remarks);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS", "Leave application #" + id + " disapproved."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // CANCEL (ADMIN)
    // -----------------------------------------------------------------------

    @PostMapping("/leave-cancel-admin/{id}")
    public String cancelAdmin(
            @PathVariable Long id,
            @RequestParam(value = "cancelReason", required = false) String cancelReason,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = getActor(request);
        if (!isAdmin(request)) return "redirect:/dashboard";
        try {
            leaveService.cancelLeave(id, actor.getId(), actor.getDisplayName(), cancelReason);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS", "Leave application #" + id + " cancelled."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // APPROVE CANCELLATION REQUEST (CANCEL_REQUESTED → CANCELLED)
    // -----------------------------------------------------------------------

    @PostMapping("/leave-cancel-approve/{id}")
    public String approveCancellation(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = getActor(request);
        if (!isAdmin(request)) return "redirect:/dashboard";
        try {
            leaveService.approveCancellation(id, actor.getId(), actor.getDisplayName(), remarks);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS",
                        "Cancellation request #" + id + " approved. Application cancelled."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // HR ACKNOWLEDGE CANCELLATION REQUEST (CANCEL_REQUESTED → CANCEL_HR_ACKNOWLEDGED)
    // Travel Leave (TL) only — intermediate step before final approval.
    // -----------------------------------------------------------------------

    @PostMapping("/leave-cancel-hr-acknowledge/{id}")
    public String hrAcknowledgeCancellation(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = getActor(request);
        if (!isAdmin(request)) return "redirect:/dashboard";
        try {
            leaveService.hrAcknowledgeCancellation(id, actor.getId(), actor.getDisplayName(), remarks);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS",
                        "Travel Leave cancellation #" + id + " acknowledged by HR. Forwarded for final approval."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // REJECT CANCELLATION REQUEST (CANCEL_REQUESTED → APPROVED)
    // -----------------------------------------------------------------------

    @PostMapping("/leave-cancel-reject/{id}")
    public String rejectCancellation(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = getActor(request);
        if (!isAdmin(request)) return "redirect:/dashboard";
        try {
            leaveService.rejectCancellation(id, actor.getId(), actor.getDisplayName(), remarks);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS",
                        "Cancellation request #" + id + " rejected. Application reverted to APPROVED."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // CANCELLATION LETTER DOWNLOAD (admin)
    // -----------------------------------------------------------------------

    @GetMapping("/leave-cancellation-letter/{id}")
    @ResponseBody
    public ResponseEntity<Resource> downloadCancellationLetter(
            @PathVariable Long id,
            HttpServletRequest request) {

        if (!isAdmin(request)) return ResponseEntity.status(403).build();

        LeaveApplication app = applicationRepo.findById(id).orElse(null);
        if (app == null || app.getCancellationLetterPath() == null
                || app.getCancellationLetterPath().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        try {
            Resource resource = fileStorageService.load(app.getCancellationLetterPath());
            String contentType = app.getCancellationLetterMimeType() != null
                    ? app.getCancellationLetterMimeType() : "application/octet-stream";
            String displayName = app.getCancellationLetterFileName() != null
                    ? app.getCancellationLetterFileName() : "cancellation-letter";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + displayName + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.warn("Cancellation letter not found for application #{}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // -----------------------------------------------------------------------
    // ATTACHMENT DOWNLOAD
    // -----------------------------------------------------------------------

    @GetMapping("/leave-attachment/{id}")
    @ResponseBody
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long id,
            HttpServletRequest request) {

        if (!isAdmin(request)) {
            return ResponseEntity.status(403).build();
        }

        LeaveApplication app = applicationRepo.findById(id).orElse(null);
        if (app == null || app.getAttachmentPath() == null || app.getAttachmentPath().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = fileStorageService.load(app.getAttachmentPath());
            String contentType = app.getAttachmentMimeType() != null
                    ? app.getAttachmentMimeType()
                    : "application/octet-stream";
            String displayName = app.getAttachmentFileName() != null
                    ? app.getAttachmentFileName()
                    : "attachment";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + displayName + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.warn("Failed to serve attachment for application #{}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // -----------------------------------------------------------------------
    // ADMIN FILE LEAVE ON BEHALF OF EMPLOYEE
    // -----------------------------------------------------------------------

    @PostMapping("/addLeaveAdmin")
    public String addLeaveAdmin(
            @RequestParam("employeeId")  Long      employeeId,
            @RequestParam("leaveTypeId") Long      leaveTypeId,
            @RequestParam("dateFrom")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("dateTo")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "reason",       required = false) String  reason,
            @RequestParam(value = "leaveSubType", required = false) String  leaveSubType,
            @RequestParam(value = "leaveDetails", required = false) String  leaveDetails,
            @RequestParam(value = "expectedReturnDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedReturnDate,
            @RequestParam(value = "requestedCommutation",
                          required = false, defaultValue = "false")  boolean requestedCommutation,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        if (!isAdmin(request)) return "redirect:/dashboard";

        // Use findByIdFetched to ensure all associations are loaded, preventing N+1 queries
        Employee  employee  = employeeRepository.findByIdFetched(employeeId).orElse(null);
        LeaveType leaveType = leaveTypeRepo.findById(leaveTypeId).orElse(null);

        if (employee == null || leaveType == null) {
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("ERROR", "Invalid employee or leave type."));
            return "redirect:/leave-management";
        }

        LeaveApplication app = new LeaveApplication();
        app.setEmployee(employee);
        app.setLeaveType(leaveType);
        app.setDateFrom(dateFrom);
        app.setDateTo(dateTo);
        app.setReason(reason);
        app.setLeaveSubType(leaveSubType);
        app.setLeaveDetails(leaveDetails);
        app.setRequestedCommutation(requestedCommutation);
        app.setExpectedReturnDate(expectedReturnDate);

        if (leaveType.isRequiresMedCert() && (attachment == null || attachment.isEmpty())) {
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("ERROR",
                            "Supporting document is required for the selected leave type."));
            return "redirect:/employee-leave/" + employee.getId() + "/LEAVE/" + employee.getEmpHashCode();
        }

        if (attachment != null && !attachment.isEmpty()) {
            try {
                String storedName = fileStorageService.store(attachment, attachment.getOriginalFilename());
                app.setAttachmentPath(storedName);
                app.setAttachmentFileName(attachment.getOriginalFilename());
                app.setAttachmentMimeType(fileStorageService.detectMimeType(attachment));
            } catch (IllegalArgumentException e) {
                redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
                return "redirect:/employee-leave/" + employee.getId() + "/LEAVE/" + employee.getEmpHashCode();
            } catch (Exception e) {
                log.warn("File upload failed: {}", e.getMessage());
                redirect.addFlashAttribute("uxmessage",
                        new UXMessage("ERROR", "File upload failed. Please try again."));
                return "redirect:/employee-leave/" + employee.getId() + "/LEAVE/" + employee.getEmpHashCode();
            }
        }

        try {
            leaveService.applyLeave(app);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS",
                            "Leave application filed for " + employee.getDisplayName() + "."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/employee-leave/" + employee.getId() + "/LEAVE/" + employee.getEmpHashCode();
    }

    // -----------------------------------------------------------------------
    // ADMIN MANUAL BALANCE ADJUSTMENT
    // -----------------------------------------------------------------------

    @PostMapping("/leave-balance-adjust")
    public String adjustBalance(
            @RequestParam("employeeId")  Long   employeeId,
            @RequestParam("leaveTypeId") Long   leaveTypeId,
            @RequestParam("year")        int    year,
            @RequestParam("days")        double days,
            @RequestParam(value = "reference", required = false) String reference,
            @RequestParam(value = "remarks",   required = false) String remarks,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        if (!isAdmin(request)) return "redirect:/dashboard";

        // Use findByIdFetched to ensure all associations are loaded, preventing N+1 queries
        Employee  employee  = employeeRepository.findByIdFetched(employeeId).orElse(null);
        LeaveType leaveType = leaveTypeRepo.findById(leaveTypeId).orElse(null);

        if (employee == null || leaveType == null) {
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("ERROR", "Invalid employee or leave type."));
            return "redirect:/leave-management";
        }
        try {
            leaveService.adjustBalance(employee, leaveType, year, days, reference, remarks);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS",
                            "Balance adjustment applied: "
                                    + (days > 0 ? "+" : "") + days + " days for "
                                    + employee.getDisplayName() + " ("
                                    + leaveType.getLeaveName() + ", " + year + ")."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/employee-leave/" + employee.getId() + "/LEAVE/" + employee.getEmpHashCode();
    }

    // -----------------------------------------------------------------------
    // ADMIN BULK ACCRUAL TRIGGER
    // Backfills accrual Jan → current month for ALL employees.
    // -----------------------------------------------------------------------

    @PostMapping("/leave-accrue-all")
    public String accrueAll(HttpServletRequest request, final RedirectAttributes redirect) {
        if (!isAdmin(request)) return "redirect:/dashboard";
        try {
            int year         = LocalDate.now().getYear();
            int currentMonth = LocalDate.now().getMonthValue();
            List<Employee> employees = employeeRepository.findAll();
            int credited = 0;
            for (Employee emp : employees) {
                for (int m = 1; m <= currentMonth; m++) {
                    try {
                        leaveService.accrueMonthlyLeave(emp, year, m);
                    } catch (Exception ignored) {
                        // Duplicate month already accrued — safe to skip
                    }
                }
                credited++;
            }
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS",
                            "Accrual complete for " + credited + " employees (Jan–"
                                    + currentMonth + " " + year + ")."));
        } catch (Exception e) {
            log.error("Bulk accrual error", e);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("ERROR", "Accrual failed: " + e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // HR VERIFY (PENDING → HR_VERIFIED) — new hierarchical workflow
    // -----------------------------------------------------------------------

    @PostMapping("/leave-hr-verify/{id}")
    public String hrVerify(
            @PathVariable Long id,
            @RequestParam(required = false) String  remarks,
            @RequestParam(required = false) Long    forwardedToId,
            @RequestParam(required = false) String  forwardedToName,
            @RequestParam(required = false) Double  certDaysWithPay,
            @RequestParam(required = false) Double  certDaysWithoutPay,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        if (!isHR(request)) return "redirect:/dashboard";
        Employee actor = getActor(request);
        try {
            leaveService.hrVerifyLeave(id, actor.getId(), actor.getDisplayName(), remarks,
                    forwardedToId, forwardedToName, certDaysWithPay, certDaysWithoutPay);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS", "Leave application #" + id + " HR-verified."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // DIVISION ENDORSE (HR_VERIFIED → ENDORSED_DIV) — new hierarchical workflow
    // -----------------------------------------------------------------------

    @PostMapping("/leave-division-endorse/{id}")
    public String divisionEndorse(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) String supervisorRecommendation,
            @RequestParam(required = false) Long   forwardedToId,
            @RequestParam(required = false) String forwardedToName,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        LeaveApplication app = applicationRepo.findById(id).orElse(null);
        if (app == null || !isDivisionEndorser(request, app)) return "redirect:/dashboard";
        Employee actor = getActor(request);
        try {
            leaveService.endorseDivisionLeave(id, actor.getId(), actor.getDisplayName(), remarks,
                    supervisorRecommendation, forwardedToId, forwardedToName);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS", "Leave application #" + id + " endorsed by Division Head."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // SECRETARY ENDORSE (ENDORSED_DIV → ENDORSED_SEC) — new hierarchical workflow
    // -----------------------------------------------------------------------

    @PostMapping("/leave-secretary-endorse/{id}")
    public String secretaryEndorse(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) Long   forwardedToId,
            @RequestParam(required = false) String forwardedToName,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        if (!isSecretary(request)) return "redirect:/dashboard";
        Employee actor = getActor(request);
        try {
            leaveService.endorseSecretaryLeave(id, actor.getId(), actor.getDisplayName(), remarks,
                    forwardedToId, forwardedToName);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS", "Leave application #" + id + " endorsed by Secretary."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/leave-management";
    }

    // -----------------------------------------------------------------------
    // ENDORSEMENT QUEUE — role-scoped view for each workflow actor
    // -----------------------------------------------------------------------

    @GetMapping("/endorsement-queue")
    public String endorsementQueue(HttpServletRequest request, HttpServletResponse response, Model model) {
        noCache(response);
        Employee actor = getActor(request);
        if (actor == null) return "redirect:/login";

        String userType = actor.getUserType();
        String queueLabel;
        java.util.List<LeaveApplication> applications;

        if ("ROLE_HR".equals(userType) || "ROLE_ADMIN".equals(userType)) {
            applications = applicationRepo.findAllActionableFetched(
                    java.util.Arrays.asList(LeaveApplication.LeaveStatus.PENDING));
            queueLabel = "Pending HR Verification";
        } else if ("ROLE_SECRETARY".equals(userType)) {
            applications = applicationRepo.findAllActionableFetched(
                    java.util.Arrays.asList(LeaveApplication.LeaveStatus.ENDORSED_DIV))
                    .stream().filter(LeaveApplication::isRequiresHigherApproval)
                    .collect(java.util.stream.Collectors.toList());
            queueLabel = "Pending Secretary Endorsement";
        } else if ("ROLE_VICE_MAYOR".equals(userType)) {
            applications = applicationRepo.findAllActionableFetched(
                    java.util.Arrays.asList(LeaveApplication.LeaveStatus.ENDORSED_DIV,
                                            LeaveApplication.LeaveStatus.ENDORSED_SEC));
            queueLabel = "Pending Vice Mayor Approval";
        } else {
            Long actorId = actor.getId();
            applications = applicationRepo.findAllActionableFetched(
                    java.util.Arrays.asList(LeaveApplication.LeaveStatus.HR_VERIFIED))
                    .stream()
                    .filter(a -> a.getEmployee() != null && a.getEmployee().getDivision() != null)
                    .filter(a -> {
                        Division div = a.getEmployee().getDivision();
                        return (div.getApprover1() != null && actorId.equals(div.getApprover1().getId()))
                            || (div.getApprover2() != null && actorId.equals(div.getApprover2().getId()));
                    })
                    .collect(java.util.stream.Collectors.toList());
            queueLabel = "Pending Division Endorsement";
        }

        List<Employee> allEmployees = employeeRepository.findAllWithAssociationsFetched();
        model.addAttribute("applications",  applications);
        model.addAttribute("queueLabel",    queueLabel);
        model.addAttribute("actorUserType", userType);
        model.addAttribute("actorId",       actor.getId());
        model.addAttribute("employees",     allEmployees);
        addEndorsementModelAttributes(model, allEmployees);
        return "employee/leave/endorsement-queue";
    }

    // -----------------------------------------------------------------------
    // STATUS HISTORY PAGE
    // -----------------------------------------------------------------------

    @GetMapping("/leave-history/{id}")
    public String viewHistory(
            @PathVariable Long id,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {

        noCache(response);
        if (!isAdmin(request)) return "redirect:/dashboard";
        LeaveApplication app = applicationRepo.findById(id).orElse(null);
        if (app == null) return "redirect:/leave-management";

        List<LeaveStatusHistory> history = java.util.Collections.emptyList();
        try {
            history = leaveService.getStatusHistory(id);
        } catch (Exception e) {
            log.warn("Failed to load history for application {}: {}", id, e.getMessage());
        }
        model.addAttribute("app",     app);
        model.addAttribute("history", history);
        return "employee/leave/leave-history";
    }

    // -----------------------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------------------

    /**
     * Populates model with three lightweight ID+name maps for endorsement modal dropdowns.
     * Uses plain Maps (not Employee objects) to avoid Jackson circular-reference issues
     * when Thymeleaf serialises these lists to inline JavaScript arrays.
     *
     *   divisionApprovers — deduplicated approver1/approver2 across all configured divisions
     *   secretaries       — employees with userType = ROLE_SECRETARY
     *   viceMayors        — employees with userType = ROLE_VICE_MAYOR
     */
    private void addEndorsementModelAttributes(Model model, List<Employee> allEmployees) {
        try {
            List<Division> divs = divisionRepository.findAllWithApprovers();
            java.util.LinkedHashMap<Long, java.util.Map<String, Object>> approverMap =
                    new java.util.LinkedHashMap<>();
            divs.forEach(d -> {
                if (d.getApprover1() != null) {
                    Employee a = d.getApprover1();
                    approverMap.put(a.getId(), empEntry(a));
                }
                if (d.getApprover2() != null) {
                    Employee a = d.getApprover2();
                    approverMap.put(a.getId(), empEntry(a));
                }
            });
            model.addAttribute("divisionApprovers", new java.util.ArrayList<>(approverMap.values()));
        } catch (Exception e) {
            log.warn("Failed to load division approvers: {}", e.getMessage());
            model.addAttribute("divisionApprovers", java.util.Collections.emptyList());
        }
        model.addAttribute("secretaries",
                allEmployees.stream()
                        .filter(e -> "ROLE_SECRETARY".equals(e.getUserType()))
                        .map(this::empEntry)
                        .collect(java.util.stream.Collectors.toList()));
        model.addAttribute("viceMayors",
                allEmployees.stream()
                        .filter(e -> "ROLE_VICE_MAYOR".equals(e.getUserType()))
                        .map(this::empEntry)
                        .collect(java.util.stream.Collectors.toList()));
    }

    /** Minimal {id, displayName} map — safe to inline as JSON without circular refs. */
    private java.util.Map<String, Object> empEntry(Employee e) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id",          e.getId());
        m.put("displayName", e.getDisplayName());
        return m;
    }

    private Employee getActor(HttpServletRequest request) {
        Employee actor = (Employee) request.getSession().getAttribute("actorObj");
        if (actor == null) {
            Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Employee) {
                actor = (Employee) auth.getPrincipal();
            }
        }
        return actor;
    }

    private boolean isAdmin(HttpServletRequest request) {
        Employee actor = getActor(request);
        return actor != null && actor.getUserType() != null
                && "ROLE_ADMIN".equals(actor.getUserType());
    }

    private boolean isHR(HttpServletRequest request) {
        Employee actor = getActor(request);
        return actor != null && ("ROLE_HR".equals(actor.getUserType())
                || "ROLE_ADMIN".equals(actor.getUserType()));
    }

    private boolean isSecretary(HttpServletRequest request) {
        Employee actor = getActor(request);
        return actor != null && ("ROLE_SECRETARY".equals(actor.getUserType())
                || "ROLE_ADMIN".equals(actor.getUserType()));
    }

    private boolean isViceMayor(HttpServletRequest request) {
        Employee actor = getActor(request);
        return actor != null && ("ROLE_VICE_MAYOR".equals(actor.getUserType())
                || "ROLE_ADMIN".equals(actor.getUserType()));
    }

    private boolean isDivisionEndorser(HttpServletRequest request, LeaveApplication app) {
        Employee actor = getActor(request);
        if (actor == null) return false;
        if ("ROLE_ADMIN".equals(actor.getUserType())) return true;
        if (app.getEmployee() == null || app.getEmployee().getDivision() == null) return false;
        Division div = app.getEmployee().getDivision();
        Long actorId = actor.getId();
        return (div.getApprover1() != null && actorId.equals(div.getApprover1().getId()))
            || (div.getApprover2() != null && actorId.equals(div.getApprover2().getId()));
    }

    private LeaveApplication buildBlankApplication(Employee employee) {
        LeaveApplication app = new LeaveApplication();
        app.setEmployee(employee);
        app.setDateFrom(LocalDate.now());
        app.setDateTo(LocalDate.now());
        return app;
    }
}
