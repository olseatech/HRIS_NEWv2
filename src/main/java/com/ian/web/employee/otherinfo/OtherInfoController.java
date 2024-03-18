package com.ian.web.employee.otherinfo;

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
import com.ian.web.employee.eligibility.CivilServiceEligibility;
import com.ian.web.employee.references.EmpReferences;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OtherInfoController {

    private final OtherInfoRepository otherInfoRepository;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/other-info/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
         if(!employeeRepository.findById(id).isPresent()){
            model.addAttribute("uxmessage", new UXMessage("ERROR", "Employee doesn't exist"));
			return "redirect:/dashboard";
        }
        Employee employee = employeeRepository.findById(id).get();
        model.addAttribute("employee", employee);
        model.addAttribute("listOfOtherInfo", otherInfoRepository.findAllByEmployee(employee));
        model.addAttribute("otherInfo", new OtherInfo());

        return "employee/pds/other-info";
    }

    @PostMapping("/save-other-info/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid OtherInfo otherInfo,
            @PathVariable("employeeId") Long id
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
			Employee employee = employeeRepository.findById(id).get();
            model.addAttribute("employee", employee);
            model.addAttribute("listOfOtherInfo", otherInfoRepository.findAllByEmployee(employee));
            model.addAttribute("otherInfo",otherInfo);

            return "employee/pds/other-info";
		}
        Employee employee = employeeRepository.findById(id).get();
        otherInfo.setEmployee(employee);

        List<OtherInfo> listOfOtherInfos = employee.getOtherInfos();
        listOfOtherInfos.add(otherInfo);
        employee.setOtherInfos(listOfOtherInfos);
        employeeRepository.save(employee);

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/other-info/"+id;
	}
}
