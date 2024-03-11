package com.ian.web.employee;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.fileupload.FileDTO;
import com.ian.web.fileupload.StorageService;
import com.ian.web.systemsettings.division.DivisionRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.position_title.PositionTitleRepository;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class EmployeeController {
	
	private final EmployeeRepository employeeRepository;	
	private final PositionTitleRepository positionTitleRepository;
	private final EmployeeStatusRepository employeeStatusRepository;
	private final DivisionRepository divisionRepository;
	private final StorageService storageService;
	
	@GetMapping("/employee/datalist")
    public ResponseEntity<FlexDatalistResult> doctorsFlexDatalist() {
        Set<Employee> allEmployeeSet = new HashSet<>(employeeRepository.findAll());
        return ResponseEntity.ok().body(new FlexDatalistResult(allEmployeeSet));
    }
	
	@GetMapping("/employee-list")
	public String listAll(Model model) {
		Iterable<Employee> employeeList = employeeRepository.findAll();
		model.addAttribute("employeeList", employeeList);
		model.addAttribute("employee", new Employee());
		return "employee/employee-list";
	}
	
	@GetMapping("/clearance-list")
	public String getAllClearance(Model model) {
		Iterable<Employee> employeeList = employeeRepository.findAll();
		model.addAttribute("employeeList", employeeList);
		model.addAttribute("employee", new Employee());
		return "employee/clearance/clearance-list";
	}
	
	@GetMapping("/employee/{employeeId}")
	public String viewEmployee(Model model, @PathVariable long employeeId) {
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		model.addAttribute("employee", employee);
		return "employee/pds/personnal-info";
	}
	
	@GetMapping("/employee/{employeeId}/{empHashCode}")
	public String viewEmployee(Model model, @PathVariable long employeeId, @PathVariable String empHashCode) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();
		if(optional.isPresent()) {			
			msg.setCode("EMP-FOUND");
			msg.setMessage("Employee Found.");
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");			
		}
		
		Employee employee = optional.orElseGet(() -> new Employee());
		model.addAttribute("employeeStatusList", employeeStatusRepository.findAll());
		model.addAttribute("divisionList", divisionRepository.findAll());
		model.addAttribute("positionTitleList", positionTitleRepository.findAll());
		model.addAttribute("employee", employee);
		model.addAttribute("msg", msg);
		return "employee/pds/personnal-info";
		
	}
	
	@PostMapping({"/addEmployee", "/editEmployee", "/saveProfile"})
	public String saveEmployee(
			@Valid Employee employee
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    
	    String uxMessageText = "Employee added successfully.";
	    String uxMessagePatientExists = "This employee already exists. This will create a new employee record.";
	    boolean editMode = false;
	    
	    Employee employeeOldRecord = null;
	    if (employee.getId() > 0) {
	    	Optional<Employee> optional = employeeRepository.findById(employee.getId());
			employeeOldRecord = optional.orElseGet(() -> new Employee());
			employee.setEmpHashCode(employeeOldRecord.getEmpHashCode());
			employee.setUsername(employeeOldRecord.getUsername());
	    	employee.setPassword(employeeOldRecord.getPassword());
	    	uxMessageText = "Employee edited successfully.";
	    	editMode = true;
	    } else {
	    	//add mode
	    	employee.setUsername(employee.getFirstName().substring(0,1) + employee.getLastName());
	    	employee.setPassword("123456");
	    }
	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("employeeList", employeeRepository.findAll());
			return "employee/employee-list";
		}
		
		List<Employee> recordMatch = employeeRepository.findByFirstNameAndLastNameAndBirthdate(
				employee.getFirstName(), employee.getLastName(), employee.getBirthdate());
		
		if (recordMatch != null && recordMatch.size() > 0) {
		    redirect.addFlashAttribute("msg", new UXMessage("ERROR", uxMessagePatientExists));
		} else {
		    redirect.addFlashAttribute("msg", new UXMessage("SUCCESS", uxMessageText));
		}
		
//		if (!request.getServletPath().equalsIgnoreCase("/addEmployee")) {
//			MultipartFile photoFile = employee.getPhotoFile();
//			String origFileName = photoFile.getOriginalFilename();
//			if(photoFile != null && origFileName != null) {
//				try {
//					String fileExt = origFileName.substring(origFileName.lastIndexOf("."));
//					String fileName = "profile_photo_" + employee.getFirstName() + "_"+ employee.getLastName() + "_" + System.currentTimeMillis() + fileExt;
//					FileDTO fileDTO = storageService.uploadFile(photoFile, fileName);
//					employee.setProfilePhoto(fileDTO.getDownloadUri());
//				} catch (Exception e) {
//					e.printStackTrace();
//				}				
//			} else {
//				employee.setProfilePhoto(null);				
//			}
//		}
		
		employee.setBirthdate(employee.getBirthdate().plusDays(1));
		
		//If Save is Add generateHasCode
		if(!editMode) {
			employee.setEmpHashCode(generateAlphanumericHash());
		}
		
		if(employeeOldRecord != null) {
			
		}
		
		if(employee.getEmpHashCode() != null && employee.getEmpHashCode().length() > 0) {
			
		} else {
			employee.setEmpHashCode(generateAlphanumericHash());
		}
		
		Employee dbPatient = employeeRepository.save(employee);
		
		if (request.getServletPath().equalsIgnoreCase("/addEmployee")) {
			return "redirect:/employee/"+dbPatient.getId()+"/"+dbPatient.getEmpHashCode();
		} else if (request.getServletPath().equalsIgnoreCase("/editEmployee")) {
			return "redirect:/employee/"+dbPatient.getId()+"/"+dbPatient.getEmpHashCode();
		} else {
			return "redirect:/employee/"+dbPatient.getId()+"/"+dbPatient.getEmpHashCode();
		}
	}
	
	public static String generateAlphanumericHash() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            char c = uuid.charAt(i);
            if (Character.isDigit(c) || Character.isLetter(c)) {
                sb.append(c);
            } else {
                sb.append((char) ('0' + (c % 10)));
            }
        }
        return sb.toString();
    }
	
}
