package com.ian.web.employee.learning;

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
import com.ian.web.employee.voluntary_workexperience.VoluntaryWork;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LearningAndDevelopmentController {
    
    private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;

    @GetMapping("/learning-development/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
        model.addAttribute("listOfLearningDevelopment", learningAndDevelopmentRepository.findAll());
        model.addAttribute("learningDevelopment", new LearningAndDevelopment());

        return "employee/pds/learning-development";
    }

    @PostMapping("/save-learning-development/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid LearningAndDevelopment learningAndDevelopment,
            @PathVariable("employeeId") Long employeeId
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
            model.addAttribute("listOfLearningDevelopment", learningAndDevelopmentRepository.findAll());
            model.addAttribute("learningDevelopment", learningAndDevelopment);
    
            return "employee/pds/learning-development";
		}

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/learning-development/"+employeeId;
	}
}
