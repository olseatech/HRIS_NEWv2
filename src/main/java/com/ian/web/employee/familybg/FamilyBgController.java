package com.ian.web.employee.familybg;

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

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FamilyBgController {
	
	private final EmployeeRepository employeeRepository;
	private final FamilyBgRepository familyBgRepository;
	
	
	@GetMapping({"/profile/familybg/{employeeId}/{empHashCode}", "/employee/familybg/{employeeId}/{empHashCode}"})
	public String viewEmployee(Model model, @PathVariable long employeeId, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();
		if(optional.isPresent()) {		
			
			Employee employee = optional.orElseGet(() -> new Employee());
			model.addAttribute("employee", employee);
			
			List<FamilyBg> familyBgList = familyBgRepository.findByEmployeeId(employeeId);
			model.addAttribute("familyBgList", familyBgList);
			
			FamilyBg familyBg = new FamilyBg();
			familyBg.setEmployee(employee);
			model.addAttribute("familyBg", familyBg );
			
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
		
		return "employee/pds/family-background";
		
	}
	
	@PostMapping({"/addFamilyBg", "/editFamilyBg"})
	public String saveFamilyBg(
			@Valid FamilyBg familyBg
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    
	    String uxMessageText = "Record added successfully.";	    
	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("familyBgList", familyBgRepository.findByEmployeeId(familyBg.getEmployee().getId()));
			return "employee/pds/family-background";
		} else {
			if(familyBg.getId() == 0 && !"CHILDREN".equalsIgnoreCase(familyBg.getRelationship())) {
				FamilyBg recordMatch = familyBgRepository.findByEmployeeIdAndRelationship(familyBg.getEmployee().getId(), familyBg.getRelationship());
				if (recordMatch != null) {
					model.addAttribute("msg", new UXMessage("ERROR", "You already have a record for your " + familyBg.getRelationship()));
					model.addAttribute("familyBgList", familyBgRepository.findByEmployeeId(familyBg.getEmployee().getId()));
					return "employee/pds/family-background";
				} else {
				    redirect.addFlashAttribute("msg", new UXMessage("SUCCESS", uxMessageText));			    
				}
			}
		}		
		
		familyBg.setBirthdate(familyBg.getBirthdate().plusDays(1));	
		
		FamilyBg dbPatient = familyBgRepository.save(familyBg);
		
		if (request.getServletPath().equalsIgnoreCase("/addFamilyBg")) {
			redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			return "redirect:/profile/familybg/"+dbPatient.getEmployee().getId()+"/"+dbPatient.getEmployee().getEmpHashCode();
		} else if (request.getServletPath().equalsIgnoreCase("/editFamilyBg")) {
			if(familyBg.getSaveMode().equalsIgnoreCase("PROFILE")) {
				redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
				return "redirect:/profile/familybg/"+dbPatient.getEmployee().getId()+"/"+dbPatient.getEmployee().getEmpHashCode();
			} else {
				redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
				return "redirect:/profile/familybg/"+dbPatient.getEmployee().getId()+"/"+dbPatient.getEmployee().getEmpHashCode();
			}			
		} else {
			return "redirect:/profile/familybg/"+dbPatient.getEmployee().getId()+"/"+dbPatient.getEmployee().getEmpHashCode();
		}
	}

}
