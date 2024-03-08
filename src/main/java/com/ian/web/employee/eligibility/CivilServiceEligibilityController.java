package com.ian.web.employee.eligibility;

import java.lang.reflect.InvocationTargetException;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CivilServiceEligibilityController {
    
    private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;

    @GetMapping("/eligibility/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
        model.addAttribute("listOfDegreeLevels", civilServiceEligibilityRepository.findAll());
        model.addAttribute("eligibility", new CivilServiceEligibility());

        return "employee/pds/eligibility";
    }

    @PostMapping("/save-eligibility/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid CivilServiceEligibility civilServiceEligibility,
            @PathVariable("employeeId") Long employeeId
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
            model.addAttribute("listOfDegreeLevels", civilServiceEligibilityRepository.findAll());

            model.addAttribute("eligibility", civilServiceEligibility);
			return "employee/pds/eligibility";
		}

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/eligibility/"+employeeId;
	}

}
