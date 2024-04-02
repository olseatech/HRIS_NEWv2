package com.ian.web.changepassword;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class ChangePasswordController {
    
    private final EmployeeRepository employeeRepository;

    @GetMapping("/change-password/{employeeId}")
    public String getPassword(
        @PathVariable("employeeId") Long id
        ,Model model){
        Employee employee = employeeRepository.findById(id).get();
        model.addAttribute("employee", employee);

        ChangePassword changePassword = new ChangePassword();
        changePassword.setEmployee(employee);
        model.addAttribute("changePassword", changePassword);
        System.out.println(changePassword.getEmployee().getFullName());
        return "/changepassword/change-password";
    }

    @PostMapping("/save-change-password")
    public String savePassword(
        @Valid ChangePassword changePassword
        ,Errors errors
		,final RedirectAttributes redirect
		,Model model
		,HttpServletRequest request
        ){
        Employee employee = changePassword.getEmployee();
        if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("changePassword", changePassword);
			return "redirect:/change-password/"+employee.getId();
		} 
        if(changePassword.getOldPassword().equals(employee.getPassword())){
            if(!changePassword.getConfirmPassword().equals(changePassword.getNewPassword())){
                model.addAttribute("msg", new UXMessage("ERROR", "Mismatch confirm password and new password."));
                model.addAttribute("changePassword", changePassword);
                return "redirect:/change-password/"+employee.getId();
            }
            employee.setPassword(changePassword.getNewPassword());
            employeeRepository.save(employee);
        }else{
            model.addAttribute("msg", new UXMessage("ERROR", "Invalid old password."));
            model.addAttribute("changePassword", changePassword);
            return "employee/pds/other-info-question";
        }
        return "redirect:/change-password/"+employee.getId();
    }
}
