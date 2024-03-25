package com.ian.web.employee.voluntary_workexperience;

import java.lang.reflect.InvocationTargetException;
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
public class VoluntaryWorkController {
    
    private final VoluntaryWorkRepository voluntaryWorkRepository;
     private final EmployeeRepository employeeRepository; 

    @GetMapping("/voluntary-work/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
        if(!employeeRepository.findById(id).isPresent()){
            model.addAttribute("uxmessage", new UXMessage("ERROR", "Employee doesn't exist"));
			return "redirect:/dashboard";
        }
        Employee employee = employeeRepository.findById(id).get();
        model.addAttribute("employee", employee);
        model.addAttribute("listOfVoluntaryWorkExperience", voluntaryWorkRepository.findAllByEmployee(employee));
        model.addAttribute("voluntaryWorkExperience", new VoluntaryWork());

        return "employee/pds/voluntary-work";
    }

    @PostMapping("/save-voluntary-work/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid VoluntaryWork voluntaryWork,
            @PathVariable("employeeId") Long id
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
            Employee employee = employeeRepository.findById(id).get();
            model.addAttribute("listOfVoluntaryWorkExperience", voluntaryWorkRepository.findAllByEmployee(employee));
            model.addAttribute("voluntaryWorkExperience", new VoluntaryWork());

            return "employee/pds/voluntary-work";
		}

        Employee employee = employeeRepository.findById(id).get();
        voluntaryWork.setEmployee(employee);
        
//        List<VoluntaryWork> voluntaryWorks = employee.getVoluntaryWorks();
//        voluntaryWorks.add(voluntaryWork);
//        employee.setVoluntaryWorks(voluntaryWorks);
//        employeeRepository.save(employee);
        
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/voluntary-work/"+id;
	}
}
