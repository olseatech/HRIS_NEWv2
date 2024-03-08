package com.ian.web.employee.workexperience;

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
public class WorkExperienceController {
    
    private final WorkExperienceRepository workExperienceRepository;

     @GetMapping("/work-experience/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
        model.addAttribute("listOfWorkExperience", workExperienceRepository.findAll());
        model.addAttribute("educationalBackground", new WorkExperience());

        return "employee/pds/work-experience";
    }

    @PostMapping("/save-work-experience/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid WorkExperience workExperience,
            @PathVariable("employeeId") Long employeeId
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
            model.addAttribute("listOfWorkExperience", workExperienceRepository.findAll());
            model.addAttribute("educationalBackground", workExperience);
    
            return "employee/pds/work-experience";
		}

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/work-experience/"+employeeId;
	}
}
