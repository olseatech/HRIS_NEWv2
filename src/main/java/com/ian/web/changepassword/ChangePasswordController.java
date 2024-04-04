package com.ian.web.changepassword;

import java.util.Objects;

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
        model.addAttribute("changePassword", changePassword);
        return "/changepassword/change-password";
    }

    @PostMapping("/save-change-password/{employeeId}")
    public String savePassword(
        @Valid ChangePassword changePassword
        ,@PathVariable("employeeId") long id
        ,Errors errors
		,final RedirectAttributes redirect
		,Model model
		,HttpServletRequest request
        ){
        Employee employee = employeeRepository.findById((long)id).get();
        if (errors.hasErrors()) {
            redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
            model.addAttribute("changePassword", changePassword);
            return "redirect:/change-password/"+id;
		} 

        if(changePassword.getConfirmPassword().equals(changePassword.getNewPassword()) && employee.getPassword().equals(changePassword.getOldPassword())){
            employee.setPassword(changePassword.getNewPassword());
            employeeRepository.save(employee);
            redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully saved."));
        }else{
            model.addAttribute("changePassword", changePassword);
            redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Invalid password."));
        }
        return "redirect:/change-password/"+id;
    }
}
