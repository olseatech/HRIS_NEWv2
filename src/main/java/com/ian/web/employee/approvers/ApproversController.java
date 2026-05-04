package com.ian.web.employee.approvers;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ApproversController {
	
//	private final EmployeeRepository employeeRepository;
	private final ClearanceApproversRepository clearanceApproversRepository;
	private final ServiceRecordSignatoryRepository serviceRecordSignatoryRepository;
	
	@GetMapping("/clearance-approver-settings")
	public String viewClearanceApprovers(Model model, HttpServletRequest request) {
		Optional<ClearanceApprovers> optional = clearanceApproversRepository.findAll().stream().findFirst();
		
		ClearanceApprovers obj = new ClearanceApprovers();
		if(optional.isPresent()) {
			obj = optional.get();		
		}
		
		model.addAttribute("clearanceApprovers", obj);		
		
		return "employee/clearance/clearance-approvers";
		
	}
	
	@GetMapping("/service-record-signatory")
	public String viewSrSignatories(Model model, HttpServletRequest request) {
		Optional<ServiceRecordSignatory> optional = serviceRecordSignatoryRepository.findAll().stream().findFirst();
		
		ServiceRecordSignatory obj = new ServiceRecordSignatory();
		if(optional.isPresent()) {
			obj = optional.get();		
		}
		
		model.addAttribute("serviceRecordSignatory", obj);		
		
		return "employee/clearance/service-record-signatory";
		
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
		
		return "redirect:/clearance-approver-settings";
	}
	
	@PostMapping("/saveClearanceApprovers")
    public String saveClearanceApprovers(@Valid ClearanceApprovers clearanceApprovers,
    		final RedirectAttributes redirect) {        
        // Save the clearance approvers
        clearanceApproversRepository.save(clearanceApprovers);
        
        // Redirect to a success page or another appropriate page
        redirect.addFlashAttribute("msg", new UXMessage("SUCCESS", "Record Successfully saved."));
        return "redirect:/clearance-approver-settings";
    }
	
	@PostMapping("/saveSrSignatory")
    public String saveSrSignatory(@Valid ServiceRecordSignatory serviceRecordSignatory,
    		final RedirectAttributes redirect) {
        
        // Save the clearance approvers
		serviceRecordSignatoryRepository.save(serviceRecordSignatory);
        
        // Redirect to a success page or another appropriate page
        redirect.addFlashAttribute("msg", new UXMessage("SUCCESS", "Record Successfully saved."));
        return "redirect:/service-record-signatory";
    }

}
