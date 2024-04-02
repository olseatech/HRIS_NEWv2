package com.ian.web.employee.clearance;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ClearanceApproversController {
	
	private final EmployeeRepository employeeRepository;
	private final ClearanceApproversRepository clearanceApproversRepository;
	
	@GetMapping("/clearance=approver-settings")
	public String viewEmployeeClearance(Model model, HttpServletRequest request) {
		Optional<ClearanceApprovers> optional = clearanceApproversRepository.findAll().stream().findFirst();
		
		ClearanceApprovers obj = new ClearanceApprovers();
		if(optional.isPresent()) {
			obj = optional.get();		
		}
		
		model.addAttribute("clearanceApprovers", obj);		
		
		return "employee/clearance/clearance-approvers";
		
	}
	
	@PostMapping({"/saveApprovers"})
	public String saveClearance(
			@Valid ClearanceApprovers clearanceApprovers
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			return "employee/clearance/clearance-approvers";
		}		
		
		clearanceApproversRepository.save(clearanceApprovers);
		
		return "redirect:/clearance=approver-settings";
	}
	
	@PostMapping("/saveClearanceApprovers")
    public String saveClearanceApprovers(@RequestParam("clearanceId") Long clearanceId, 
    		@RequestParam("selectedApprovers") List<Long> selectedApprovers,
    		final RedirectAttributes redirect) {
        // Create ClearanceApprovers object
        ClearanceApprovers clearanceApprovers = new ClearanceApprovers();
        clearanceApprovers.setId(clearanceId); // Set clearance ID
        
        // Add selected approvers to the clearance approvers list
        for (Long approverId : selectedApprovers) {
            Employee approver = employeeRepository.findById(approverId).orElseThrow(() -> new IllegalArgumentException("Invalid employee ID"));
            clearanceApprovers.addApprover(approver);
        }
        
        // Save the clearance approvers
        clearanceApproversRepository.save(clearanceApprovers);
        
        // Redirect to a success page or another appropriate page
        redirect.addFlashAttribute("msg", new UXMessage("SUCCESS", "Record Successfully saved."));
        return "redirect:/clearance=approver-settings";
    }

}
