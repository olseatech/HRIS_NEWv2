package com.ian.web.employee.eligibility;

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
import com.ian.web.systemsettings.eligibility.EligibilityRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CivilServiceEligibilityController {
    
    private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final EmployeeRepository employeeRepository;
	private final EligibilityRepository eligibilityRepository;

    @GetMapping({"/profile/civil-eligibility/{employeeId}/{empHashCode}", "/employee/civil-eligibility/{employeeId}/{empHashCode}"})
	public String viewEmployee(Model model, @PathVariable long employeeId, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();

		if(optional.isPresent()) {		
			
			Employee employee = optional.orElseGet(() -> new Employee());
			model.addAttribute("employee", employee);
			
			List<CivilServiceEligibility> civilEligibilityList = civilServiceEligibilityRepository.findByEmployeeId(employee.getId());
			model.addAttribute("civilEligibilityList", civilEligibilityList);
			model.addAttribute("eligibilityList", eligibilityRepository.findAll());
			
			CivilServiceEligibility civilServiceEligibility = new CivilServiceEligibility();
			civilServiceEligibility.setEmployee(employee);
			model.addAttribute("civilServiceEligibility", civilServiceEligibility );
			
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
		
		return "employee/pds/eligibility";
		
	}
	
	@PostMapping({"/addCivilEligibility", "/editCivilEligibility"})
	public String saveFamilyBg(
			@Valid CivilServiceEligibility civilServiceEligibility
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    
	    String uxMessageText = "Record added successfully.";	    
	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("civilEligibilityList", civilServiceEligibilityRepository.findByEmployeeId(civilServiceEligibility.getEmployee().getId()));
			return "employee/pds/eligibility";
		} 
        // else {
		// 	if(familyBg.getId() == 0 && !"CHILDREN".equalsIgnoreCase(familyBg.getRelationship())) {
		// 		FamilyBg recordMatch = familyBgRepository.findByEmployeeIdAndRelationship(familyBg.getEmployee().getId(), familyBg.getRelationship());
		// 		if (recordMatch != null) {
		// 			model.addAttribute("msg", new UXMessage("ERROR", "You already have a record for your " + familyBg.getRelationship()));
		// 			model.addAttribute("familyBgList", familyBgRepository.findByEmployeeId(familyBg.getEmployee().getId()));
		// 			return "employee/pds/family-background";
		// 		} else {
		// 		    redirect.addFlashAttribute("msg", new UXMessage("SUCCESS", uxMessageText));			    
		// 		}
		// 	}
		// }		
		civilServiceEligibility = civilServiceEligibilityRepository.save(civilServiceEligibility);

		if (request.getServletPath().equalsIgnoreCase("/addWorkExperience")) {
			redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			return "redirect:/profile/civil-eligibility/"+civilServiceEligibility.getEmployee().getId()+"/"+civilServiceEligibility.getEmployee().getEmpHashCode();
		} else if (request.getServletPath().equalsIgnoreCase("/editWorkExperience")) {
			// if(workExperience.getSaveMode().equalsIgnoreCase("PROFILE")) {
			// 	redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			// 	return "redirect:/profile/work-experience/"+workExperience.getEmployee().getId()+"/"+workExperience.getEmployee().getEmpHashCode();
			// } else {
			// 	redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			// 	return "redirect:/profile/work-experience/"+workExperience.getEmployee().getId()+"/"+workExperience.getEmployee().getEmpHashCode();
			// }			
            redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			return "redirect:/profile/civil-eligibility/"+civilServiceEligibility.getEmployee().getId()+"/"+civilServiceEligibility.getEmployee().getEmpHashCode();
		} else {
			return "redirect:/profile/civil-eligibility/"+civilServiceEligibility.getEmployee().getId()+"/"+civilServiceEligibility.getEmployee().getEmpHashCode();
		}
	}

}
