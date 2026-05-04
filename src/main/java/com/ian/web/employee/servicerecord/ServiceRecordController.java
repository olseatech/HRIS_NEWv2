package com.ian.web.employee.servicerecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.position_title.PositionTitleRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ServiceRecordController {
	
	private final EmployeeRepository employeeRepository;
	private final ServiceRecordRepository serviceRecordRepository;
	private final EmployeeStatusRepository employeeStatusRepository;
	private final PositionTitleRepository positionTitleRepository;
	
	@GetMapping("/employee-service-record/{employeeId}/{empHashCode}")
	@Transactional
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
		model.addAttribute("positionTitleList", positionTitleRepository.findAllByOrderByPositionTitleNameAsc());
		
//		List<ServiceRecord> serviceRecordList = serviceRecordRepository.findByEmployeeId(employeeId);
		
		List<ServiceRecord> serviceRecordList = serviceRecordRepository.findByEmployeeIdOrderByDateFromDesc(employeeId);
		
		serviceRecordList.forEach(serviceRecordTemp -> {
			
			if(serviceRecordTemp.getDateFrom() != null) {
				serviceRecordTemp.setDateFrom(serviceRecordTemp.getDateFrom().plusDays(1));
			}
			
			
			if(serviceRecordTemp.getDateTo() != null) {
				serviceRecordTemp.setDateTo(serviceRecordTemp.getDateTo().plusDays(1));
			}
			
			
			if(serviceRecordTemp.getEntranceDate() != null) {
				LocalDate newDate = serviceRecordTemp.getEntranceDate().plusDays(1);
				serviceRecordTemp.setEntranceDate(newDate);
			}
			
			if(serviceRecordTemp.getSeparationDate() != null) {
				serviceRecordTemp.setSeparationDate(serviceRecordTemp.getSeparationDate().plusDays(1));
			}
			
			
			if(serviceRecordTemp.getSigningDate() != null) {
				serviceRecordTemp.setSigningDate(serviceRecordTemp.getSigningDate().plusDays(1));
			}
			
			if(serviceRecordTemp.getPublicationDateFrom() != null) {
				serviceRecordTemp.setPublicationDateFrom(serviceRecordTemp.getPublicationDateFrom().plusDays(1));
			}
			
			if(serviceRecordTemp.getPublicationDateTo() != null) {
				serviceRecordTemp.setPublicationDateTo(serviceRecordTemp.getPublicationDateTo().plusDays(1));
			}
			
			if(serviceRecordTemp.getDateOfActionCscAction() != null) {
				serviceRecordTemp.setDateOfActionCscAction(serviceRecordTemp.getDateOfActionCscAction().plusDays(1));
			}
			
			if(serviceRecordTemp.getDateOfReleaseCscAction() != null) {
				serviceRecordTemp.setDateOfReleaseCscAction(serviceRecordTemp.getDateOfReleaseCscAction().plusDays(1));
			}
			
		});
		
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
			model.addAttribute("serviceRecordList", serviceRecordRepository.findByEmployeeIdOrderByDateFromDesc(serviceRecord.getEmployee().getId()));
			
			if (request.getServletPath().equalsIgnoreCase("/addMyServiceRecord")) {
				return "employee/service-record/my-service-record";
			} else {
				return "employee/service-record/employee-service-record";
			}
			
		} 
				
//		if(serviceRecord.getDateFrom() != null) {
//			serviceRecord.setDateFrom(serviceRecord.getDateFrom().plusDays(1));
//		}
//		
//		
//		if(serviceRecord.getDateTo() != null) {
//			serviceRecord.setDateTo(serviceRecord.getDateTo().plusDays(1));
//		}
//		
//		
//		if(serviceRecord.getEntranceDate() != null) {
//			serviceRecord.setEntranceDate(serviceRecord.getEntranceDate().plusDays(1));
//		}
//		
//		if(serviceRecord.getSeparationDate() != null) {
//			serviceRecord.setSeparationDate(serviceRecord.getSeparationDate().plusDays(1));
//		}
//		
//		
//		if(serviceRecord.getSigningDate() != null) {
//			serviceRecord.setSigningDate(serviceRecord.getSigningDate().plusDays(1));
//		}
//		
//		if(serviceRecord.getPublicationDateFrom() != null) {
//			serviceRecord.setPublicationDateFrom(serviceRecord.getPublicationDateFrom().plusDays(1));
//		}
//		
//		if(serviceRecord.getPublicationDateTo() != null) {
//			serviceRecord.setPublicationDateTo(serviceRecord.getPublicationDateTo().plusDays(1));
//		}
//		
//		if(serviceRecord.getDateOfActionCscAction() != null) {
//			serviceRecord.setDateOfActionCscAction(serviceRecord.getDateOfActionCscAction().plusDays(1));
//		}
//		
//		if(serviceRecord.getDateOfReleaseCscAction() != null) {
//			serviceRecord.setDateOfReleaseCscAction(serviceRecord.getDateOfReleaseCscAction().plusDays(1));
//		}
		
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
