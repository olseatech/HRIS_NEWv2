package com.ian.web.employee.workexperience;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

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

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WorkExperienceController {
    
    private final WorkExperienceRepository workExperienceRepository;
    private final EmployeeRepository employeeRepository;

     @GetMapping("/work-experience/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
        if(!employeeRepository.findById(id).isPresent()){
            model.addAttribute("uxmessage", new UXMessage("ERROR", "Employee doesn't exist"));
			return "redirect:/dashboard";
        }
        Employee employee = employeeRepository.findById(id).get();
        model.addAttribute("employee", employee);
        model.addAttribute("listOfWorkExperience", workExperienceRepository.findAllByEmployee(employee));
        model.addAttribute("workExperience", new WorkExperience());

        return "employee/pds/work-experience";
    }

    @PostMapping("/save-work-experience/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid WorkExperience workExperience,
            @PathVariable("employeeId") Long id
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
            Employee employee = employeeRepository.findById(id).get();
            model.addAttribute("employee", employee);
            model.addAttribute("listOfWorkExperience", workExperienceRepository.findAll());
            model.addAttribute("workExperience", workExperience);

            return "employee/pds/work-experience";
		}

        Employee employee = employeeRepository.findById(id).get();
        workExperience.setEmployee(employee);

//        List<WorkExperience> listOfWorkExperience = employee.getWorkExperiences();
//        listOfWorkExperience.add(workExperience);
//        employee.setWorkExperiences(listOfWorkExperience);
//        employeeRepository.save(employee);

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/work-experience/"+id;
	}
}
