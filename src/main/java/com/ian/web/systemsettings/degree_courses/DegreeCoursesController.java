package com.ian.web.systemsettings.degree_courses;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.systemsettings.academichonors.AcademicHonors;


@Controller
@RequiredArgsConstructor
public class DegreeCoursesController {
    private final DegreeCoursesRepository degreeCoursesRepository;

    @GetMapping("/degree-courses")
    public String getData(Model model) {
        Iterable<DegreeCourses> listOfDegreeCourses = degreeCoursesRepository.findAll();
        model.addAttribute("listOfDegreeCourses", listOfDegreeCourses);
        model.addAttribute("degreeCourse", new DegreeCourses());
        return "system-settings/degree-courses/degree-courses-list";
    }

    @PostMapping("/save-degree-courses")
	@Transactional
	public String getRecord(
			@Valid DegreeCourses degreeCourses
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/degree-courses/degree-courses-list";
		}
				
		degreeCoursesRepository.save(degreeCourses);
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/degree-courses";
	}
    
}
