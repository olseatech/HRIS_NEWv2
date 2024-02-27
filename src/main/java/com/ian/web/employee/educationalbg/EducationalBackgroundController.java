package com.ian.web.employee.educationalbg;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.systemsettings.academichonors.AcademicHonorsRepository;
import com.ian.web.systemsettings.degree_courses.DegreeCoursesRepository;
import com.ian.web.systemsettings.degreelevels.DegreeLevelRepository;
import com.ian.web.systemsettings.scholarship.ScholarshipRepository;
import com.ian.web.systemsettings.schools.SchoolRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EducationalBackgroundController {
    
    private final EducationalBackgroundRepository educationalBackgroundRepository;
    private final DegreeLevelRepository degreeLevelRepository;
    private final SchoolRepository schoolRepository;
    private final DegreeCoursesRepository degreeCoursesRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final AcademicHonorsRepository academicHonorsRepository;

    @GetMapping("/educational-background")
    public String getRecord(Model model){
        model.addAttribute("listOfDegreeLevels", degreeLevelRepository.findAll());
        model.addAttribute("listOfSchools", schoolRepository.findAll());
        model.addAttribute("listOfDegreeCourses", degreeCoursesRepository.findAll());
        model.addAttribute("listOfScholarship", scholarshipRepository.findAll());
        model.addAttribute("listOfAcademicHonors", academicHonorsRepository.findAll());
        model.addAttribute("educationalBackground", new EducationalBackgroundModel());

        return "employee/educational-background/educational-background-list";
    }

    @PostMapping("/save-educational-background")
    @Transactional
	public String getRecord(
			@Valid EducationalBackgroundModel educationalBackgroundModel
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "employee/educational-background/educational-background-list";
		}
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/educational-background";
	}

}
