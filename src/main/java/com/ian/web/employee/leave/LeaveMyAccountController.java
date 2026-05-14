package com.ian.web.employee.leave;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Employee self-service leave controller.
 *
 * Routes:
 *   GET  /my-leave               — view balances, applications, file leave
 *   POST /my-leave-apply         — submit a new leave application
 *   POST /my-leave-cancel/{id}   — cancel own PENDING application
 *   POST /my-leave-resubmit/{id} — re-submit a RETURNED application
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LeaveMyAccountController {

    private final LeaveApplicationRepository applicationRepo;
    private final LeaveTypeRepository        leaveTypeRepo;
    private final LeaveService               leaveService;
    private final EmployeeRepository         employeeRepository;
    private final FileStorageService         fileStorageService;

    // -----------------------------------------------------------------------
    // VIEW — /my-leave
    // -----------------------------------------------------------------------

@GetMapping("/my-leave")
    public String myLeave(Model model, HttpServletRequest request,
                          HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setBufferSize(65536);

        Employee actor = getActor(request);
        if (actor == null) return "redirect:/login";

        try {
            // Use findByIdFetched (JOIN FETCH all EAGER associations in one query)
            // so that division, positionTitle, district, and employeeStatus are all
            // initialised before Thymeleaf starts rendering — eliminates N+1 SELECTs
            // and any risk of LazyInitializationException mid-render.
            Employee employee = employeeRepository.findByIdFetched((long) actor.getId()).orElse(null);
            if (employee == null) return "redirect:/dashboard";

            // Pre-compute association fields that may be lazy-loaded.
            // With spring.jpa.open-in-view=false the Hibernate session is closed before
            // Thymeleaf renders, so accessing lazy proxies in the template throws
            // LazyInitializationException and causes ERR_INCOMPLETE_CHUNKED_ENCODING.
            // Catching here gives a safe null fallback instead of a mid-stream crash.
            String positionTitleName = safeGet(() -> employee.getPositionTitle() != null
                    ? employee.getPositionTitle().getPositionTitleName() : null);
            String divisionName = safeGet(() -> employee.getDivision() != null
                    ? employee.getDivision().getDivisionName() : null);

            int year = LocalDate.now().getYear();
            List<LeaveBalance> balances = java.util.Collections.emptyList();
            try {
                balances = leaveService.getBalancesForEmployee(employee.getId(), year);
            } catch (Exception e) {
                log.warn("Failed to load balances for employee {}: {}", employee.getId(), e.getMessage());
            }

            // Auto-initialise: credit accrual Jan → current month on first visit
            if (balances.isEmpty()) {
                int currentMonth = LocalDate.now().getMonthValue();
                for (int m = 1; m <= currentMonth; m++) {
                    try {
                        leaveService.accrueMonthlyLeave(employee, year, m);
                    } catch (Exception ignored) { }
                }
                try {
                    balances = leaveService.getBalancesForEmployee(employee.getId(), year);
                } catch (Exception e) {
                    log.warn("Failed to reload balances after accrual: {}", e.getMessage());
                    balances = java.util.Collections.emptyList();
                }
            }

            List<LeaveApplication> applications = java.util.Collections.emptyList();
            try {
                applications = applicationRepo.findByEmployeeIdFetched(employee.getId());
                // Force initialization of all associations while session is open
                for (LeaveApplication app : applications) {
                    if (app.getLeaveType() != null) {
                        app.getLeaveType().getLeaveCode();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to load applications: {}", e.getMessage());
            }

            List<LeaveType> leaveTypes = java.util.Collections.emptyList();
            try {
                leaveTypes = leaveTypeRepo.findByActiveTrueOrderBySortOrderAscLeaveNameAsc();
            } catch (Exception e) {
                log.warn("Failed to load leave types: {}", e.getMessage());
            }

            long returnedCount = 0;
            try {
                returnedCount = applications.stream()
                        .filter(a -> a.getStatus() == LeaveApplication.LeaveStatus.RETURNED)
                        .count();
            } catch (Exception ignored) { }

            model.addAttribute("employee",           employee);
            model.addAttribute("positionTitleName",  positionTitleName);
            model.addAttribute("divisionName",        divisionName);
            model.addAttribute("balances",            balances);
            model.addAttribute("vlBalance",           findBalanceByCode(balances, "VL"));
            model.addAttribute("slBalance",           findBalanceByCode(balances, "SL"));
            model.addAttribute("flBalance",           findBalanceByCode(balances, "FL"));
            model.addAttribute("splBalance",          findBalanceByCode(balances, "SPL"));
            model.addAttribute("applications",        applications);
            model.addAttribute("leaveTypes",          leaveTypes);
            model.addAttribute("currentYear",         year);
            model.addAttribute("returnedCount",       returnedCount);
            model.addAttribute("actorId",             employee.getId());
        } catch (Exception e) {
            log.error("My Leave page error", e);
            // CRITICAL: Never set employee to null - template relies on it for display.
            // Use actor (guaranteed non-null at this point)
            model.addAttribute("employee",           actor);
            model.addAttribute("positionTitleName",  null);
            model.addAttribute("divisionName",        null);
            model.addAttribute("balances",            java.util.Collections.emptyList());
            model.addAttribute("vlBalance",           null);
            model.addAttribute("slBalance",           null);
            model.addAttribute("flBalance",           null);
            model.addAttribute("splBalance",          null);
            model.addAttribute("applications",        java.util.Collections.emptyList());
            model.addAttribute("leaveTypes",          java.util.Collections.emptyList());
            model.addAttribute("currentYear",         LocalDate.now().getYear());
            model.addAttribute("returnedCount",       0L);
            model.addAttribute("uxmessage",
                new UXMessage("ERROR",
                    "Unable to load leave data. Please try again or contact IT support."));
        }
        return "my-account/my-leave";
    }

    private LeaveBalance findBalanceByCode(List<LeaveBalance> balances, String code) {
        if (balances == null || code == null) {
            return null;
        }
        for (LeaveBalance balance : balances) {
            if (balance != null && balance.getLeaveType() != null
                    && code.equalsIgnoreCase(balance.getLeaveType().getLeaveCode())) {
                return balance;
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // POST — file a new leave application
    // -----------------------------------------------------------------------

    @PostMapping("/my-leave-apply")
    public String applyLeave(
            @RequestParam("leaveTypeId")   Long      leaveTypeId,
            @RequestParam("dateFrom")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("dateTo")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "leaveSubType",   required = false) String  leaveSubType,
            @RequestParam(value = "leaveDetails",   required = false) String  leaveDetails,
            @RequestParam(value = "reason",         required = false) String  reason,
            @RequestParam(value = "expectedReturnDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedReturnDate,
            @RequestParam(value = "requestedCommutation",
                          required = false, defaultValue = "false") boolean requestedCommutation,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = getActor(request);
        if (actor == null) return "redirect:/login";

        // Use findByIdFetched to ensure all associations are pre-loaded, preventing N+1 queries
        // and lazy initialization errors if an error page is rendered.
        Employee  employee  = employeeRepository.findByIdFetched(actor.getId()).orElse(null);
        LeaveType leaveType = leaveTypeRepo.findById(leaveTypeId).orElse(null);

        if (employee == null || leaveType == null) {
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("ERROR", "Invalid leave type. Please try again."));
            return "redirect:/my-leave";
        }

        LeaveApplication app = new LeaveApplication();
        app.setEmployee(employee);
        app.setLeaveType(leaveType);
        app.setDateFrom(dateFrom);
        app.setDateTo(dateTo);
        app.setLeaveSubType(leaveSubType);
        app.setLeaveDetails(leaveDetails);
        app.setReason(reason);
        app.setExpectedReturnDate(expectedReturnDate);
        app.setRequestedCommutation(requestedCommutation);

        // Extended leave document requirement (e.g. SL >5 days requires medical cert)
        if (leaveType.isRequiresDocForExtended() && leaveType.getExtendedLeaveDays() > 0) {
            double previewDays = leaveService.countWorkingDays(dateFrom, dateTo);
            if (previewDays > leaveType.getExtendedLeaveDays()
                    && (attachment == null || attachment.isEmpty())) {
                redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR",
                    "A supporting document is required for "
                    + leaveType.getLeaveName() + " exceeding "
                    + leaveType.getExtendedLeaveDays() + " working days."));
                return "redirect:/my-leave";
            }
        }

        // Enforce required attachment for medical/flagged leave types
        if (leaveType.isRequiresMedCert() && (attachment == null || attachment.isEmpty())) {
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("ERROR",
                            "Supporting document is required for the selected leave type."));
            return "redirect:/my-leave";
        }

        // Handle file attachment (optional unless leave type requires medical cert)
        if (attachment != null && !attachment.isEmpty()) {
            try {
                String storedName = fileStorageService.store(attachment, attachment.getOriginalFilename());
                app.setAttachmentPath(storedName);
                app.setAttachmentFileName(attachment.getOriginalFilename());
                app.setAttachmentMimeType(fileStorageService.detectMimeType(attachment));
            } catch (IllegalArgumentException e) {
                redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
                return "redirect:/my-leave";
            } catch (Exception e) {
                log.warn("File upload failed: {}", e.getMessage());
                redirect.addFlashAttribute("uxmessage",
                        new UXMessage("ERROR", "File upload failed. Please try again."));
                return "redirect:/my-leave";
            }
        }

        try {
            leaveService.applyLeave(app);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS", "Leave application submitted. Pending HR review."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/my-leave";
    }

    // -----------------------------------------------------------------------
    // POST — cancel own PENDING application
    // -----------------------------------------------------------------------

    @PostMapping("/my-leave-cancel/{id}")
    public String cancelOwnLeave(
            @PathVariable Long id,
            @RequestParam(value = "cancelReason", required = false) String cancelReason,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = getActor(request);
        if (actor == null) return "redirect:/login";

        LeaveApplication app = applicationRepo.findById(id).orElse(null);
        if (app == null || app.getEmployee().getId() != actor.getId()) {
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("ERROR", "Application not found or does not belong to you."));
            return "redirect:/my-leave";
        }
        if (app.getStatus() != LeaveApplication.LeaveStatus.PENDING) {
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("ERROR", "Only PENDING applications can be cancelled."));
            return "redirect:/my-leave";
        }
        try {
            leaveService.cancelLeave(id, actor.getId(), actor.getDisplayName(), cancelReason);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS", "Leave application cancelled."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/my-leave";
    }

    // -----------------------------------------------------------------------
    // ATTACHMENT VIEW (employee views own attachment)
    // -----------------------------------------------------------------------

    @GetMapping("/my-leave-attachment/{id}")
    @ResponseBody
    public ResponseEntity<Resource> viewAttachment(
            @PathVariable Long id,
            HttpServletRequest request) {

        Employee actor = getActor(request);
        if (actor == null) return ResponseEntity.status(401).build();

        LeaveApplication app = applicationRepo.findById(id).orElse(null);
        if (app == null || app.getEmployee().getId() != actor.getId()) {
            return ResponseEntity.status(403).build();
        }
        if (app.getAttachmentPath() == null || app.getAttachmentPath().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = fileStorageService.load(app.getAttachmentPath());
            String contentType = app.getAttachmentMimeType() != null
                    ? app.getAttachmentMimeType() : "application/octet-stream";
            String displayName = app.getAttachmentFileName() != null
                    ? app.getAttachmentFileName() : "attachment";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + displayName + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.warn("Attachment not found for application #{}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // -----------------------------------------------------------------------
    // POST — re-submit a RETURNED application after correction
    // -----------------------------------------------------------------------

    @PostMapping("/my-leave-resubmit/{id}")
    public String resubmitLeave(
            @PathVariable Long id,
            @RequestParam(value = "reason",       required = false) String reason,
            @RequestParam(value = "leaveDetails", required = false) String leaveDetails,
            @RequestParam(value = "leaveSubType", required = false) String leaveSubType,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = getActor(request);
        if (actor == null) return "redirect:/login";

        LeaveApplication app = applicationRepo.findById(id).orElse(null);
        if (app == null || app.getEmployee().getId() != actor.getId()) {
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("ERROR", "Application not found or does not belong to you."));
            return "redirect:/my-leave";
        }
        if (app.getStatus() != LeaveApplication.LeaveStatus.RETURNED) {
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("ERROR", "Only RETURNED applications can be re-submitted."));
            return "redirect:/my-leave";
        }
        try {
            leaveService.resubmitLeave(id, actor.getId(), reason, leaveDetails, leaveSubType);
            redirect.addFlashAttribute("uxmessage",
                    new UXMessage("SUCCESS", "Leave application re-submitted for HR review."));
        } catch (Exception e) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", e.getMessage()));
        }
        return "redirect:/my-leave";
    }

    // -----------------------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------------------

    private Employee getActor(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Employee actor = null;

        if (session != null) {
            actor = (Employee) session.getAttribute("actorObj");
        }

        if (actor == null) {
            Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Employee) {
                actor = (Employee) auth.getPrincipal();
                if (session != null) {
                    session.setAttribute("actorObj", actor);
                }
            }
        }
        return actor;
    }

    /**
     * Safely invokes a supplier that might throw LazyInitializationException (or any
     * other exception) when accessing a detached Hibernate proxy outside a session.
     * Returns null instead of crashing — callers must handle null in templates.
     */
    private static <T> T safeGet(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }
}
