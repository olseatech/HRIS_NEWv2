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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.eligibility.CivilServiceEligibilityRepository;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestionRepository;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperienceRepository;
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
	
	private final FamilyBgRepository familyBgRepository;
	private final EducationalBackgroundRepository educationalBackgroundRepository;
	private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final WorkExperienceRepository workExperienceRepository;
	private final VoluntaryWorkRepository voluntaryWorkRepository;
	private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
	private final OtherInfoRepository otherInfoRepository;
	private final OtherInfoQuestionRepository otherInfoQuestionRepository;
	private final EmpReferencesRepository empReferencesRepository;
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;
	
	@GetMapping("/employee/datalist")
    public ResponseEntity<FlexDatalistResult> doctorsFlexDatalist() {
        Set<Employee> allEmployeeSet = new HashSet<>(employeeRepository.findAll());
        return ResponseEntity.ok().body(new FlexDatalistResult(allEmployeeSet));
    }
	
	@GetMapping("/employee-list")
	public String listAll(Model model) {
		Iterable<Employee> employeeList = employeeRepository.findAll();
		model.addAttribute("employeeList", employeeList);
		model.addAttribute("employeeStatusList", employeeStatusRepository.findAll());
		model.addAttribute("divisionList", divisionRepository.findAll());
		model.addAttribute("positionTitleList", positionTitleRepository.findAll());
		model.addAttribute("employee", new Employee());
		return "employee/employee-list";
	}
	
	
	
//	@GetMapping("/employee/{employeeId}")
//	public String viewEmployee(Model model, @PathVariable long employeeId) {
//		Employee employee = employeeRepository.findById(employeeId).orElseGet(()->new Employee());
//		System.out.println("\n\n\n\n\n"+employee.getEducationalBackgrounds()+"\n\n\n\n\n");
//		model.addAttribute("employee", employee);
//		return "employee/pds/personnal-info";
//	}
	
	
	@GetMapping("/employee/{employeeId}/{showMode}/{empHashCode}")
	//@GetMapping({"/profile/{employeeId}/{empHashCode}", "/employee/{employeeId}/{empHashCode}"})
	public String viewEmployee(Model model, @PathVariable long employeeId, @PathVariable String showMode, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();
		if(optional.isPresent()) {			
			
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}
		
		Employee employee = optional.orElseGet(() -> new Employee());
		model.addAttribute("employeeStatusList", employeeStatusRepository.findAll());
		model.addAttribute("divisionList", divisionRepository.findAll());
		model.addAttribute("positionTitleList", positionTitleRepository.findAll());
		
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
	    	if(employee.getProfilePhoto() != null && employee.getProfilePhoto().length() > 0) {
	    		
	    	} else {
	    		if(employeeOldRecord.getProfilePhoto() != null && employeeOldRecord.getProfilePhoto().length() > 0) {
	    			employee.setProfilePhoto(employeeOldRecord.getProfilePhoto());
	    		}
	    	}
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
				
		if(employee.getEmpHashCode() != null && employee.getEmpHashCode().length() > 0) {
			
		} else {
			employee.setEmpHashCode(generateAlphanumericHash());
		}
		
		String showMode = employee.getShowMode();
		employee = employeeRepository.save(employee);
		
		if (request.getServletPath().equalsIgnoreCase("/addEmployee")) {
			redirect.addFlashAttribute("msg", new UXMessage("ADD-SUCCESS", "Employee Record Successfully Saved."));
			return "redirect:/employee-list";
		} else {
			redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Employee Record Successfully Updated."));
			return "redirect:/employee/"+employee.getId()+"/"+showMode+"/"+employee.getEmpHashCode();
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
