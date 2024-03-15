package com.ian.web.employee.educationalbg;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
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
    private final EmployeeRepository employeeRepository;

    @GetMapping("/educational-background/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") long id){
        if(!employeeRepository.findById(id).isPresent()){
            model.addAttribute("uxmessage", new UXMessage("ERROR", "Employee doesn't exist"));
			return "redirect:/dashboard";
        }
        Employee employee = employeeRepository.findById(id).get();
        model.addAttribute("listOfDegreeLevels", degreeLevelRepository.findAll());
        model.addAttribute("listOfSchools", schoolRepository.findAll());
        model.addAttribute("listOfDegreeCourses", degreeCoursesRepository.findAll());
        model.addAttribute("listOfScholarships", scholarshipRepository.findAll());
        model.addAttribute("listOfAcademicHonors", academicHonorsRepository.findAll());
        model.addAttribute("listOfEducationalBackground", educationalBackgroundRepository.findAllByEmployee(employee));

        model.addAttribute("employee", employee);
        model.addAttribute("educationalBackground", new EducationalBackgroundModel());

        return "employee/pds/educ-background";
    }

    @PostMapping("/save-educational-background/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid EducationalBackgroundModel educationalBackgroundModel
            ,@PathVariable("employeeId") long id
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
			Employee employee = employeeRepository.findById(id).get();
            model.addAttribute("listOfDegreeLevels", degreeLevelRepository.findAll());
            model.addAttribute("listOfSchools", schoolRepository.findAll());
            model.addAttribute("listOfDegreeCourses", degreeCoursesRepository.findAll());
            model.addAttribute("listOfScholarships", scholarshipRepository.findAll());
            model.addAttribute("listOfAcademicHonors", academicHonorsRepository.findAll());
            model.addAttribute("listOfEducationalBackground", educationalBackgroundRepository.findAllByEmployee(employee));


            model.addAttribute("employee", employeeRepository.findById(id).get());
            model.addAttribute("educationalBackground", educationalBackgroundModel);
			return "employee/pds/educ-background";
		}

        Employee employee = employeeRepository.findById(id).orElseGet(()->new Employee());
        EducationalBackground educationalBackground = new EducationalBackground();
        BeanUtils.copyProperties(educationalBackground, educationalBackgroundModel);
        educationalBackground.setDegreeLevel(degreeLevelRepository.findById(educationalBackgroundModel.getDegreeLevelId()).get());
        educationalBackground.setSchool(schoolRepository.findById(educationalBackgroundModel.getSchoolId()).get());
        educationalBackground.setDegreeCourse(degreeCoursesRepository.findById(educationalBackgroundModel.getDegreeCourseId()).get());
        educationalBackground.setScholarship(scholarshipRepository.findById(educationalBackgroundModel.getScholarshipId()).get());
        educationalBackground.setAcademicHonors(academicHonorsRepository.findById(educationalBackgroundModel.getAcademicHonorsId()).get());
        educationalBackground.setEmployee(employee);

        List<EducationalBackground> listOfEmployeeEducationalBackground = employee.getEducationalBackgrounds();
        listOfEmployeeEducationalBackground.add(educationalBackground);
        employee.setEducationalBackgrounds(listOfEmployeeEducationalBackground);
        employeeRepository.save(employee);

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/educational-background/"+id;
	}

}
