package com.ian.web.employee.voluntary_workexperience;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
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
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.eligibility.CivilServiceEligibility;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class VoluntaryWorkController {
    
     private final VoluntaryWorkRepository voluntaryWorkRepository;
     private final EmployeeRepository employeeRepository; 

    @GetMapping({"/profile/voluntary-work/{employeeId}/{empHashCode}", "/employee/voluntary-work/{employeeId}/{empHashCode}"})
	public String viewEmployee(Model model, @PathVariable long employeeId, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();

		if(optional.isPresent()) {		
			
			Employee employee = optional.orElseGet(() -> new Employee());
			model.addAttribute("employee", employee);
			
			List<VoluntaryWork> voluntaryWorkList = voluntaryWorkRepository.findByEmployeeId(employeeId);
			model.addAttribute("voluntaryWorkList", voluntaryWorkList);
			
			VoluntaryWork voluntaryWork = new VoluntaryWork();
			voluntaryWork.setEmployee(employee);
			model.addAttribute("voluntaryWork", voluntaryWork);
			
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
		
		return "employee/pds/voluntary-work";
		
	}
	
	@PostMapping({"/addVoluntaryWork", "/editVoluntaryWork"})
	public String saveFamilyBg(
			@Valid VoluntaryWork voluntaryWork
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    
	    String uxMessageText = "Record added successfully.";	    
	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("voluntaryWorkList", voluntaryWorkRepository.findByEmployeeId(voluntaryWork.getEmployee().getId()));
			return "employee/pds/voluntary-work";
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
		voluntaryWork = voluntaryWorkRepository.save(voluntaryWork);

		if (request.getServletPath().equalsIgnoreCase("/addWorkExperience")) {
			redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			return "redirect:/profile/voluntary-work/"+voluntaryWork.getEmployee().getId()+"/"+voluntaryWork.getEmployee().getEmpHashCode();
		} else if (request.getServletPath().equalsIgnoreCase("/editWorkExperience")) {
			// if(workExperience.getSaveMode().equalsIgnoreCase("PROFILE")) {
			// 	redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			// 	return "redirect:/profile/work-experience/"+workExperience.getEmployee().getId()+"/"+workExperience.getEmployee().getEmpHashCode();
			// } else {
			// 	redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			// 	return "redirect:/profile/work-experience/"+workExperience.getEmployee().getId()+"/"+workExperience.getEmployee().getEmpHashCode();
			// }			
            redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			return "redirect:/profile/voluntary-work/"+voluntaryWork.getEmployee().getId()+"/"+voluntaryWork.getEmployee().getEmpHashCode();
		} else {
			return "redirect:/profile/voluntary-work/"+voluntaryWork.getEmployee().getId()+"/"+voluntaryWork.getEmployee().getEmpHashCode();
		}
	}

}
