package com.ian.web.employee.eligibility;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
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
import com.ian.web.systemsettings.eligibility.Eligibility;
import com.ian.web.systemsettings.eligibility.EligibilityRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CivilServiceEligibilityController {
    
    private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final EmployeeRepository employeeRepository;
	private final EligibilityRepository eligibilityRepository;

    @GetMapping("/eligibility/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
		if(!employeeRepository.findById(id).isPresent()){
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Employee doesn't exist"));
			return "redirect:/dashboard";
		}
		Employee employee = employeeRepository.findById(id).get();
		model.addAttribute("employee", employee);
		model.addAttribute("listOfEligibility", eligibilityRepository.findAll());
        model.addAttribute("listOfDegreeLevels", civilServiceEligibilityRepository.findAllByEmployee(employee));
        model.addAttribute("eligibility", new CivilServiceEligibilityModel());

        return "employee/pds/eligibility";
    }

    @PostMapping("/save-eligibility/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid CivilServiceEligibilityModel civilServiceEligibilityModel,
            @PathVariable("employeeId") Long id
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
			Employee employee = employeeRepository.findById(id).get();
			model.addAttribute("employee", employee);
			model.addAttribute("listOfEligibility", eligibilityRepository.findAll());
			model.addAttribute("listOfDegreeLevels", civilServiceEligibilityRepository.findAllByEmployee(employee));
			model.addAttribute("eligibility", new CivilServiceEligibilityModel());

			return "employee/pds/eligibility";
		}

		Employee employee = employeeRepository.findById(id).get();

		CivilServiceEligibility civilServiceEligibility = new CivilServiceEligibility();
		BeanUtils.copyProperties(civilServiceEligibilityModel, civilServiceEligibility);
		civilServiceEligibility.setEligibility(eligibilityRepository.findById(civilServiceEligibilityModel.getEligibilityId()).get());
		civilServiceEligibility.setEmployee(employee);
		civilServiceEligibilityRepository.save(civilServiceEligibility);

		List<CivilServiceEligibility> listOfEligibility = employee.getCivilServiceEligibilities();
		listOfEligibility.add(civilServiceEligibility);
		employee.setCivilServiceEligibilities(listOfEligibility);
		employeeRepository.save(employee);
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/eligibility/"+id;
	}

}
