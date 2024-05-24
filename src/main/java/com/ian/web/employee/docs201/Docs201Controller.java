package com.ian.web.employee.docs201;

import java.util.ArrayList;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.fileupload.FileDTO;
import com.ian.web.fileupload.StorageService;
import com.ian.web.systemsettings.document_type.DocumentType;
import com.ian.web.systemsettings.document_type.DocumentTypeRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class Docs201Controller {
	
	private final EmployeeRepository employeeRepository;
	private final Docs201Repository docs201Repository;
	private final DocumentTypeRepository documentTypeRepository;
	private final StorageService storageService;
	
	@GetMapping("/201files/{employeeId}/{empHashCode}")
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
		
		Docs201 docs201 = new Docs201();
		docs201.setEmployee(employee);
		docs201.setId(0L);
		
		model.addAttribute("docs201", docs201);
		
		List<Docs201> docsList = docs201Repository.findByEmployeeId(employeeId);
		model.addAttribute("docsList", docsList);
		
		List<DocumentType> documentTypeList = documentTypeRepository.findAll();
		model.addAttribute("documentTypeList", documentTypeList);
		
		model.addAttribute("showMode", "HRADMIN");		
		
		return "employee/docs201/docs201";
		
	}
	
	@GetMapping("/my201files/{employeeId}/{empHashCode}")
	public String viewMyEmployeeClearance(Model model, @PathVariable long employeeId, @PathVariable String empHashCode, HttpServletRequest request) {
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
		
		Docs201 docs201 = new Docs201();
		docs201.setEmployee(employee);
		docs201.setId(0L);
		
		model.addAttribute("docs201", docs201);
		
		List<Docs201> docsList = docs201Repository.findByEmployeeId(employeeId);
		model.addAttribute("docsList", docsList);
		
		List<DocumentType> documentTypeList = documentTypeRepository.findAll();
		model.addAttribute("documentTypeList", documentTypeList);
		
		model.addAttribute("showMode", "PROFILE");				
		
		return "employee/docs201/docs201";
		
	}
	
	@PostMapping({"/addMy201Files", "/add201Files"})
	public String save201Files(
			@Valid Docs201 docs201
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
		
		List<String> docFileUrls =new ArrayList<>();
        long empId = docs201.getEmployee().getId();
        if (errors.hasErrors()  || docs201.getDocumentType().getId() <= 0) {
        	if (docs201.getDocumentType().getId() <= 0) {
                errors.rejectValue("documentType", "documentType.id.zero", " is mandatory");
            }
            model.addAttribute("isValidationErrorOnAdd", true);
        	
            List<Docs201> docsList = this.docs201Repository.findByEmployeeId(empId);
            List<DocumentType> documentTypeList = this.documentTypeRepository.findAll();
            Optional<Employee> optionalEmployee = this.employeeRepository.findById(empId);
            Employee employee = optionalEmployee.get();
            model.addAttribute("employee", employee);
            model.addAttribute("docsList", docsList);
            model.addAttribute("documentTypeList", documentTypeList);
            model.addAttribute("docs201", docs201);
                
            return "employee/docs201/docs201";
        }		
		
		MultipartFile[] files = docs201.getDocFiles();
		if(files.length > 0 && files[0].getOriginalFilename().isEmpty()) {
			Docs201 docObj = docs201Repository.findById(docs201.getId()).orElseGet(() -> new Docs201());
			docFileUrls = docObj.getDocFileUrls();
		} else {
		    for (int i = 0; i < files.length; i++) {
		    	try {
		    		String fileExt = files[i].getOriginalFilename().substring(files[i].getOriginalFilename().lastIndexOf("."));
		    		String fileName = "emp_201file_" + empId + '_' + i + "_" + System.currentTimeMillis() + fileExt;
		    		FileDTO fileDTO = storageService.uploadFile(files[i], fileName);
		    		docFileUrls.add(fileDTO.getDownloadUri());
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    	}
		    }
		}
		docs201.setDocFileUrls(docFileUrls);
		Employee employee = employeeRepository.findById(empId).orElseGet(() -> new Employee());
		docs201.setEmployee(employee);
		DocumentType docType = documentTypeRepository.findById(docs201.getDocumentType().getId())
				    .orElseGet(() -> new DocumentType());
		docs201.setDocumentType(docType);			
		
		docs201Repository.save(docs201);
		
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
		return "redirect:/201files/"+docs201.getEmployee().getId()+"/"+docs201.getEmployee().getEmpHashCode();
	}

}
