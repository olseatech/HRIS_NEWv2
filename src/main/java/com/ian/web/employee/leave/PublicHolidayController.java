package com.ian.web.employee.leave;

import java.time.LocalDate;
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
 * System Settings — Public Holiday management (admin only).
 *
 * Routes:
 *   GET  /public-holidays           — list all holidays
 *   POST /addPublicHoliday          — create/edit holiday
 *   GET  /deletePublicHoliday/{id}  — delete holiday
 */
@Controller
@RequiredArgsConstructor
public class PublicHolidayController {

    private final PublicHolidayRepository holidayRepo;

    @GetMapping("/public-holidays")
    public String list(
            @RequestParam(value = "year", required = false) Integer year,
            Model model,
            HttpServletRequest request) {
        if (!isAdmin(request)) return "redirect:/dashboard";
        int displayYear = (year != null) ? year : LocalDate.now().getYear();
        model.addAttribute("holidays",     year != null
                ? holidayRepo.findByYear(displayYear)
                : holidayRepo.findAllByOrderByHolidayDateAsc());
        model.addAttribute("newHoliday",   new PublicHoliday());
        model.addAttribute("selectedYear", displayYear);
        model.addAttribute("currentYear",  LocalDate.now().getYear());
        return "system-settings/holidays/holiday-list";
    }

    @PostMapping({"/addPublicHoliday", "/editPublicHoliday"})
    public String save(
            @Valid PublicHoliday holiday,
            Errors errors,
            Model model,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        if (!isAdmin(request)) return "redirect:/dashboard";

        // Duplicate date check (on add only)
        if (holiday.getId() == null && holidayRepo.existsByHolidayDate(holiday.getHolidayDate())) {
            errors.rejectValue("holidayDate", "duplicate", "A holiday already exists on this date.");
        }

        if (errors.hasErrors()) {
            model.addAttribute("msg",      new UXMessage("ERROR", "Please fix the items marked in red."));
            model.addAttribute("newHoliday", holiday);
            model.addAttribute("holidays", holidayRepo.findAllByOrderByHolidayDateAsc());
            int selectedYear = holiday.getHolidayDate() != null
                    ? holiday.getHolidayDate().getYear()
                    : LocalDate.now().getYear();
            model.addAttribute("selectedYear", selectedYear);
            model.addAttribute("currentYear", LocalDate.now().getYear());
            return "system-settings/holidays/holiday-list";
        }

        holidayRepo.save(holiday);
        redirect.addFlashAttribute("msg",
                new UXMessage("EDIT-SUCCESS", "Holiday saved successfully."));
        return "redirect:/public-holidays";
    }

    @GetMapping("/deletePublicHoliday/{id}")
    public String delete(
            @PathVariable Long id,
            HttpServletRequest request,
            final RedirectAttributes redirect) {

        if (!isAdmin(request)) return "redirect:/dashboard";
        try {
            holidayRepo.deleteById(id);
            redirect.addFlashAttribute("msg",
                    new UXMessage("EDIT-SUCCESS", "Holiday deleted."));
        } catch (Exception e) {
            redirect.addFlashAttribute("msg",
                    new UXMessage("ERROR", "Cannot delete this holiday record."));
        }
        return "redirect:/public-holidays";
    }

    private boolean isAdmin(HttpServletRequest request) {
        Employee actor = (Employee) request.getSession().getAttribute("actorObj");
        return actor != null && "ROLE_ADMIN".equals(actor.getUserType());
    }
}
