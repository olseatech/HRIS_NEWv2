package com.ian.web.employee.leave;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import lombok.RequiredArgsConstructor;

/**
 * System Settings — Leave Types (admin only).
 * Routes: /leave-types, /addLeaveType, /editLeaveType, /deleteLeaveType/{id}
 */
@Controller
@RequiredArgsConstructor
public class LeaveTypeSettingsController {

    private final LeaveTypeRepository leaveTypeRepo;

    @GetMapping("/leave-types")
    public String list(Model model, HttpServletRequest request) {
        Employee actor = (Employee) request.getSession().getAttribute("actorObj");
        if (actor == null || !"ROLE_ADMIN".equals(actor.getUserType())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("leaveTypes", leaveTypeRepo.findAllByOrderBySortOrderAscLeaveNameAsc());
        model.addAttribute("newLeaveType", new LeaveType());
        return "system-settings/leave-types/leave-type-list";
    }

    @PostMapping({"/addLeaveType", "/editLeaveType"})
    public String save(
            @Valid LeaveType leaveType,
            Errors errors,
            Model model,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = (Employee) request.getSession().getAttribute("actorObj");
        if (actor == null || !"ROLE_ADMIN".equals(actor.getUserType())) {
            return "redirect:/dashboard";
        }

        // Duplicate code check
        boolean duplicate = leaveType.getId() == null
                ? leaveTypeRepo.existsByLeaveCode(leaveType.getLeaveCode())
                : leaveTypeRepo.existsByLeaveCodeAndIdNot(leaveType.getLeaveCode(), leaveType.getId());

        if (duplicate) {
            errors.rejectValue("leaveCode", "duplicate", "Leave code already exists.");
        }

        if (errors.hasErrors()) {
            model.addAttribute("msg", new UXMessage("ERROR", "Please fix the items marked in red."));
            model.addAttribute("leaveTypes", leaveTypeRepo.findAllByOrderBySortOrderAscLeaveNameAsc());
            return "system-settings/leave-types/leave-type-list";
        }

        leaveTypeRepo.save(leaveType);
        redirect.addFlashAttribute("msg",
                new UXMessage("EDIT-SUCCESS", "Leave type saved successfully."));
        return "redirect:/leave-types";
    }

    @GetMapping("/deleteLeaveType/{id}")
    public String delete(
            @PathVariable Long id,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        Employee actor = (Employee) request.getSession().getAttribute("actorObj");
        if (actor == null || !"ROLE_ADMIN".equals(actor.getUserType())) {
            return "redirect:/dashboard";
        }
        try {
            leaveTypeRepo.deleteById(id);
            redirect.addFlashAttribute("msg",
                    new UXMessage("EDIT-SUCCESS", "Leave type deleted."));
        } catch (Exception e) {
            redirect.addFlashAttribute("msg",
                    new UXMessage("ERROR", "Cannot delete — leave type may be in use."));
        }
        return "redirect:/leave-types";
    }
}
