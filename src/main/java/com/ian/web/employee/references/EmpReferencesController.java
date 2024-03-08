package com.ian.web.employee.references;

import java.lang.reflect.InvocationTargetException;

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

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EmpReferencesController {

    private final EmpReferencesRepository empReferencesRepository;

    @GetMapping("/references/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
        model.addAttribute("listOfReference", empReferencesRepository.findAll());
        model.addAttribute("reference", new EmpReferences());

        return "employee/pds/references";
    }

    @PostMapping("/save-references/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid EmpReferences empReferences,
            @PathVariable("employeeId") Long employeeId
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
            model.addAttribute("listOfReference", empReferencesRepository.findAll());
            model.addAttribute("reference", new EmpReferences());
    
            return "employee/pds/references";
        }

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/references/"+employeeId;
	}

}
