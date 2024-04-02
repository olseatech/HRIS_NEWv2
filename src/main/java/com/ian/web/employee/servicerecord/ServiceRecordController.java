package com.ian.web.employee.servicerecord;

import java.time.LocalDate;
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
import com.ian.web.employee.clearance.Clearance;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ServiceRecordController {
	
	private final EmployeeRepository employeeRepository;
	private final ServiceRecordRepository serviceRecordRepository;
	private final EmployeeStatusRepository employeeStatusRepository;
	
	@GetMapping("/employee-service-record/{employeeId}/{empHashCode}")
	public String viewEmployeeClearance(Model model, @PathVariable long employeeId, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();
		if(optional.isPresent()) {			
			
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}
		
		Employee employee = optional.orElseGet(() -> new Employee());
		
		model.addAttribute("employee", employee);
		
		ServiceRecord serviceRecord = new ServiceRecord();
		serviceRecord.setEmployee(employee);
		
		model.addAttribute("serviceRecord", serviceRecord);
		model.addAttribute("employeeStatusList", employeeStatusRepository.findAll());
		
		List<ServiceRecord> serviceRecordList = serviceRecordRepository.findByEmployeeId(employeeId);
		model.addAttribute("serviceRecordList", serviceRecordList);
		
		if (request.getServletPath().startsWith("/profile")) {
			model.addAttribute("showMode", "PROFILE");
		} else {
			model.addAttribute("showMode", "HRADMIN");
		}		
		
		
		return "employee/service-record/employee-service-record";
		
	}
	
	@GetMapping("/my-service-record/{employeeId}/{empHashCode}")
	public String viewMyClearance(Model model, @PathVariable long employeeId, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();
		if(optional.isPresent()) {			
			
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}
		
		Employee employee = optional.orElseGet(() -> new Employee());
		
		model.addAttribute("employee", employee);
		
		ServiceRecord serviceRecord = new ServiceRecord();
		serviceRecord.setEmployee(employee);
		
		model.addAttribute("serviceRecord", serviceRecord);
		model.addAttribute("employeeStatusList", employeeStatusRepository.findAll());
		
		List<ServiceRecord> serviceRecordList = serviceRecordRepository.findByEmployeeId(employeeId);
		model.addAttribute("serviceRecordList", serviceRecordList);
		
		if (request.getServletPath().startsWith("/profile")) {
			model.addAttribute("showMode", "PROFILE");
		} else {
			model.addAttribute("showMode", "HRADMIN");
		}		
		
		
		return "employee/service-record/my-service-record";
		
	}
	
	@PostMapping({"/addMyServiceRecord", "/addServiceRecord"})
	public String saveServiceRecord(
			@Valid ServiceRecord serviceRecord
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("serviceRecordList", serviceRecordRepository.findByEmployeeId(serviceRecord.getEmployee().getId()));
			
			if (request.getServletPath().equalsIgnoreCase("/addMyServiceRecord")) {
				return "employee/service-record/my-service-record";
			} else {
				return "employee/service-record/employee-service-record";
			}
			
		} 
				
		
		serviceRecord = serviceRecordRepository.save(serviceRecord);
		
		if (request.getServletPath().equalsIgnoreCase("/addMyServiceRecord")) {
			redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			return "redirect:/my-service-record/"+serviceRecord.getEmployee().getId()+"/"+serviceRecord.getEmployee().getEmpHashCode();
		} else {
			redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
			return "redirect:/employee-service-record/"+serviceRecord.getEmployee().getId()+"/"+serviceRecord.getEmployee().getEmpHashCode();
		}
	}

}
