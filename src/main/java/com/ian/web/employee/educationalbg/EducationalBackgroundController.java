package com.ian.web.employee.educationalbg;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
    
    @GetMapping({"/profile/educationalbg/{employeeId}/{empHashCode}", "/employee/educationalbg/{employeeId}/{empHashCode}"})
    public String getRecord(Model model, @PathVariable long employeeId, @PathVariable String empHashCode, HttpServletRequest request){
    	Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();
		if(optional.isPresent()) {		
			
			Employee employee = optional.orElseGet(() -> new Employee());
			model.addAttribute("employee", employee);
			
			List<EducationalBackground> educationalBgList = educationalBackgroundRepository.findByEmployeeId(employeeId);
			model.addAttribute("educationalBgList", educationalBgList);
			
			EducationalBackground educationalBg = new EducationalBackground();
			educationalBg.setEmployee(employee);
			model.addAttribute("educationalBg", educationalBg );
			
			if (request.getServletPath().startsWith("/profile")) {
				model.addAttribute("showMode", "PROFILE");
			} else {
				model.addAttribute("showMode", "HRADMIN");
			}
			
			
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}		
		
		return "employee/pds/educational-background";
    }
    
    @PostMapping({"/addEducationalBg", "/editEducationalBg"})
	public String saveEducationalBg(
			@Valid EducationalBackground educBackground
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    
	    String uxMessageText = "Record added successfully.";
	    String uxMessagePatientExists = "This record already exists.";
	    boolean editMode = false;	    
	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("educationalBgList", educationalBackgroundRepository.findByEmployeeId(educBackground.getEmployee().getId()));
			return "employee/pds/educational-background";
		} 		
				
		
		EducationalBackground dbPatient = educationalBackgroundRepository.save(educBackground);
		
		if (request.getServletPath().equalsIgnoreCase("/addFamilyBg")) {
			redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			return "redirect:/profile/educationalbg/"+dbPatient.getEmployee().getId()+"/"+dbPatient.getEmployee().getEmpHashCode();
		} else if (request.getServletPath().equalsIgnoreCase("/editFamilyBg")) {
			if(educBackground.getSaveMode().equalsIgnoreCase("PROFILE")) {
				redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
				return "redirect:/profile/educationalbg/"+dbPatient.getEmployee().getId()+"/"+dbPatient.getEmployee().getEmpHashCode();
			} else {
				redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
				return "redirect:/profile/educationalbg/"+dbPatient.getEmployee().getId()+"/"+dbPatient.getEmployee().getEmpHashCode();
			}			
		} else {
			return "redirect:/profile/educationalbg/"+dbPatient.getEmployee().getId()+"/"+dbPatient.getEmployee().getEmpHashCode();
		}
	}
    

//    @GetMapping("/educational-background/{employeeId}")
//    public String getRecord(Model model, @PathVariable("employeeId") long id){
//        if(!employeeRepository.findById(id).isPresent()){
//            model.addAttribute("uxmessage", new UXMessage("ERROR", "Employee doesn't exist"));
//			return "redirect:/dashboard";
//        }
//        Employee employee = employeeRepository.findById(id).get();
//        model.addAttribute("listOfDegreeLevels", degreeLevelRepository.findAll());
//        model.addAttribute("listOfSchools", schoolRepository.findAll());
//        model.addAttribute("listOfDegreeCourses", degreeCoursesRepository.findAll());
//        model.addAttribute("listOfScholarships", scholarshipRepository.findAll());
//        model.addAttribute("listOfAcademicHonors", academicHonorsRepository.findAll());
//        model.addAttribute("listOfEducationalBackground", educationalBackgroundRepository.findAllByEmployee(employee));
//
//        model.addAttribute("employee", employee);
//        model.addAttribute("educationalBackground", new EducationalBackgroundModel());
//
//        return "employee/pds/educ-background";
//    }

//    @PostMapping("/save-educational-background/{employeeId}")
//    @Transactional
//	public String getRecord(
//			@Valid EducationalBackgroundModel educationalBackgroundModel
//            ,@PathVariable("employeeId") long id
//			,Errors errors
//			,final RedirectAttributes redirect
//			,Model model
//			) throws IllegalAccessException, InvocationTargetException {
//		if (errors.hasErrors()) {
//			Employee employee = employeeRepository.findById(id).get();
//            model.addAttribute("listOfDegreeLevels", degreeLevelRepository.findAll());
//            model.addAttribute("listOfSchools", schoolRepository.findAll());
//            model.addAttribute("listOfDegreeCourses", degreeCoursesRepository.findAll());
//            model.addAttribute("listOfScholarships", scholarshipRepository.findAll());
//            model.addAttribute("listOfAcademicHonors", academicHonorsRepository.findAll());
//            model.addAttribute("listOfEducationalBackground", educationalBackgroundRepository.findAllByEmployee(employee));
//
//
//            model.addAttribute("employee", employeeRepository.findById(id).get());
//            model.addAttribute("educationalBackground", educationalBackgroundModel);
//			return "employee/pds/educ-background";
//		}
//
//        Employee employee = employeeRepository.findById(id).orElseGet(()->new Employee());
//        EducationalBackground educationalBackground = new EducationalBackground();
//        BeanUtils.copyProperties(educationalBackground, educationalBackgroundModel);
////        educationalBackground.setDegreeLevel(degreeLevelRepository.findById(educationalBackgroundModel.getDegreeLevelId()).get());
////        educationalBackground.setSchool(schoolRepository.findById(educationalBackgroundModel.getSchoolId()).get());
////        educationalBackground.setDegreeCourse(degreeCoursesRepository.findById(educationalBackgroundModel.getDegreeCourseId()).get());
////        educationalBackground.setScholarship(scholarshipRepository.findById(educationalBackgroundModel.getScholarshipId()).get());
////        educationalBackground.setAcademicHonors(academicHonorsRepository.findById(educationalBackgroundModel.getAcademicHonorsId()).get());
//        educationalBackground.setEmployee(employee);
//
////        List<EducationalBackground> listOfEmployeeEducationalBackground = employee.getEducationalBackgrounds();
////        listOfEmployeeEducationalBackground.add(educationalBackground);
////        employee.setEducationalBackgrounds(listOfEmployeeEducationalBackground);
//        employeeRepository.save(employee);
//
//		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
//		return "redirect:/educational-background/"+id;
//	}

}
