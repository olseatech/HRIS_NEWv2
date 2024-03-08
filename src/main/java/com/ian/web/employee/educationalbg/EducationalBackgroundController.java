package com.ian.web.employee.educationalbg;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
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
        model.addAttribute("listOfScholarships", scholarshipRepository.findAll());
        model.addAttribute("listOfAcademicHonors", academicHonorsRepository.findAll());
        model.addAttribute("listOfEducationalBackground", educationalBackgroundRepository.findAll());
        model.addAttribute("educationalBackground", new EducationalBackgroundModel());

        return "employee/pds/educ-background";
    }

    @PostMapping("/save-educational-background")
    @Transactional
	public String getRecord(
			@Valid EducationalBackgroundModel educationalBackgroundModel
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
            model.addAttribute("listOfDegreeLevels", degreeLevelRepository.findAll());
            model.addAttribute("listOfSchools", schoolRepository.findAll());
            model.addAttribute("listOfDegreeCourses", degreeCoursesRepository.findAll());
            model.addAttribute("listOfScholarships", scholarshipRepository.findAll());
            model.addAttribute("listOfAcademicHonors", academicHonorsRepository.findAll());
            model.addAttribute("listOfEducationalBackground", educationalBackgroundRepository.findAll());

            model.addAttribute("educationalBackground", educationalBackgroundModel);
			return "employee/pds/educ-background";
		}

        EducationalBackground educationalBackground = null;
        if(Objects.isNull(educationalBackgroundModel.getId())){
            educationalBackground = new EducationalBackground();
        }else { educationalBackground = educationalBackgroundRepository.findById(educationalBackgroundModel.getId()).get(); }

        BeanUtils.copyProperties(educationalBackground, educationalBackgroundModel);
        educationalBackground.setDegreeLevel(degreeLevelRepository.findById(educationalBackgroundModel.getDegreeLevelId()).get());
        educationalBackground.setSchool(schoolRepository.findById(educationalBackgroundModel.getSchoolId()).get());
        educationalBackground.setDegreeCourse(degreeCoursesRepository.findById(educationalBackgroundModel.getDegreeCourseId()).get());
        educationalBackground.setScholarship(scholarshipRepository.findById(educationalBackgroundModel.getScholarshipId()).get());
        educationalBackground.setAcademicHonors(academicHonorsRepository.findById(educationalBackgroundModel.getAcademicHonorsId()).get());

        educationalBackgroundRepository.save(educationalBackground);

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/educational-background";
	}

}
