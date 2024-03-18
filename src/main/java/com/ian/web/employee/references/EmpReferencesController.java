package com.ian.web.employee.references;

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
public class EmpReferencesController {

    private final EmpReferencesRepository empReferencesRepository;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/references/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
        if(!employeeRepository.findById(id).isPresent()){
            model.addAttribute("uxmessage", new UXMessage("ERROR", "Employee doesn't exist"));
			return "redirect:/dashboard";
        }
        Employee employee = employeeRepository.findById(id).get();
        model.addAttribute("employee", employee);
        model.addAttribute("listOfReferences", empReferencesRepository.findAllByEmployee(employee));
        model.addAttribute("empReference", new EmpReferences());

        return "employee/pds/references";
    }

    @PostMapping("/save-references/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid EmpReferences empReferences,
            @PathVariable("employeeId") Long id
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
            Employee employee = employeeRepository.findById(id).get();
            model.addAttribute("employee", employee);
            model.addAttribute("listOfReferences", empReferencesRepository.findAllByEmployee(employee));
            model.addAttribute("empReference", empReferences);

            return "employee/pds/references";
        }

        Employee employee = employeeRepository.findById(id).get();
        empReferences.setEmployee(employee);

        System.out.println("\n\n\n\n\nstart\n\n\n\n\n");

        List<EmpReferences> listOfReference = employee.getEmpReferences();
        listOfReference.add(empReferences);
        employee.setEmpReferences(listOfReference);
        employeeRepository.save(employee);

        System.out.println("\n\n\n\n\nsave\n\n\n\n\n");

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/references/"+id;
	}

}
