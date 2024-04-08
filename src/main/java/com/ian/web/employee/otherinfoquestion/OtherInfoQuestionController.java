package com.ian.web.employee.otherinfoquestion;

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
import com.ian.web.employee.PdsCountDto;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.eligibility.CivilServiceEligibility;
import com.ian.web.employee.eligibility.CivilServiceEligibilityRepository;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperienceRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OtherInfoQuestionController {
    
    private final OtherInfoQuestionRepository otherInfoQuestionRepository;
    private final EmployeeRepository employeeRepository;
    private final FamilyBgRepository familyBgRepository;
    private final EducationalBackgroundRepository educationalBackgroundRepository;
	private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final WorkExperienceRepository workExperienceRepository;
	private final VoluntaryWorkRepository voluntaryWorkRepository;
	private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
	private final EmpReferencesRepository empReferencesRepository;
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;
    private final OtherInfoRepository otherInfoRepository;

    @GetMapping("/employee/other-info-question/{employeeId}/{showMode}/{empHashCode}")
    //@GetMapping({"/profile/civil-eligibility/{employeeId}/{empHashCode}", "/employee/civil-eligibility/{employeeId}/{empHashCode}"})
	public String viewEmployee(Model model, @PathVariable long employeeId, @PathVariable String showMode, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();

		if(optional.isPresent()) {		
			
			Employee employee = optional.orElseGet(() -> new Employee());
			
			PdsCountDto pdsDtoCount = new PdsCountDto();
			pdsDtoCount.setFamilyBgCount(familyBgRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setEducationalBgCount(educationalBackgroundRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setEligibilityCount(civilServiceEligibilityRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setWorkExperienceCount(workExperienceRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setVoluntaryWorkCount(voluntaryWorkRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setLearningDevCount(learningAndDevelopmentRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setOtherInfoCount(otherInfoRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setOtherInfoQuestionsCount(otherInfoQuestionRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setReferencesCount(empReferencesRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setGovIdCount(govermentIssuedIdRepository.findByEmployeeId(employeeId).size());
			
			employee.setPdsCountDto(pdsDtoCount);
			employee.setShowMode(showMode);
			
			model.addAttribute("employee", employee);
			
			List<OtherInfoQuestion> otherInfoQuestionList = otherInfoQuestionRepository.findByEmployeeId(employee.getId());
			
			OtherInfoQuestion otherInfoQuestion;

			// Check if the list contains any elements
			if (!otherInfoQuestionList.isEmpty()) {
			    // If the list is not empty, get the first record
			    otherInfoQuestion = otherInfoQuestionList.get(0);
			} else {
			    // If the list is empty, create a new OtherInfoQuestion object
			    otherInfoQuestion = new OtherInfoQuestion();
			}
			
            otherInfoQuestion.setEmployee(employee);
            otherInfoQuestion.setShowMode(showMode);
			model.addAttribute("otherInfoQuestion", otherInfoQuestion );
						
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}		
		
		return "employee/pds/other-info-question";
		
	}
	
	@PostMapping({"/addOtherInfoQuestion", "/editOtherInfoQuestion"})
	public String saveFamilyBg(
			@Valid OtherInfoQuestion otherInfoQuestion
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("otherInfoQuestion", otherInfoQuestionRepository.findByEmployeeId(otherInfoQuestion.getEmployee().getId()));
			return "employee/pds/other-info-question";
		} 
        	
		String showMode = otherInfoQuestion.getShowMode();
		otherInfoQuestion = otherInfoQuestionRepository.save(otherInfoQuestion);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
		return "redirect:/employee/other-info-question/"+otherInfoQuestion.getEmployee().getId()+"/"+showMode+"/"+otherInfoQuestion.getEmployee().getEmpHashCode();
	}

}
