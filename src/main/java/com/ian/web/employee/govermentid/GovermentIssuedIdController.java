package com.ian.web.employee.govermentid;

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
import com.ian.web.employee.learning.LearningAndDevelopment;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class GovermentIssuedIdController {

    private final GovermentIssuedIdRepository govermentIssuedIdRepository;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/government-issuedId/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
      if(!employeeRepository.findById(id).isPresent()){
            model.addAttribute("uxmessage", new UXMessage("ERROR", "Employee doesn't exist"));
			return "redirect:/dashboard";
        }
        Employee employee = employeeRepository.findById(id).get();
        model.addAttribute("employee", employee);
        model.addAttribute("listOfGovernmentIssuedId", govermentIssuedIdRepository.findAllByEmployee(employee));
        model.addAttribute("governmentIssuedId", new GovermentIssuedId());

        return "employee/pds/gov-issued-id";
    }

    @PostMapping("/save-government-issuedId/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid GovermentIssuedId govermentIssuedId,
            @PathVariable("employeeId") Long id
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
            Employee employee = employeeRepository.findById(id).get();
            model.addAttribute("employee", employee);
            model.addAttribute("listOfGovernmentIssuedId", govermentIssuedIdRepository.findAllByEmployee(employee));
            model.addAttribute("governmentIssuedId", govermentIssuedId);

            return "employee/pds/gov-issued-id";
		}

        Employee employee = employeeRepository.findById(id).get();
        
        govermentIssuedId.setEmployee(employee);
        
//        List<GovermentIssuedId> listOfGovernmentId = employee.getGovermentIssuedIds();
//        listOfGovernmentId.add(govermentIssuedId);
//        employee.setGovermentIssuedIds(listOfGovernmentId);
//        employeeRepository.save(employee);

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/government-issuedId/"+id;
	}

}
