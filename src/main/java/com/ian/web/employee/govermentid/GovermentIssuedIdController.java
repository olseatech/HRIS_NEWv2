package com.ian.web.employee.govermentid;

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
import com.ian.web.employee.learning.LearningAndDevelopment;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class GovermentIssuedIdController {
    private final GovermentIssuedIdRepository govermentIssuedIdRepository;

    @GetMapping("/government-issuedId/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
        model.addAttribute("listOfGovernmentIssuedId", govermentIssuedIdRepository.findAll());
        model.addAttribute("governmentIssuedId", new GovermentIssuedId());

        return "employee/pds/gov-issued-id";
    }

    @PostMapping("/save-government-issuedId/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid GovermentIssuedId govermentIssuedId,
            @PathVariable("employeeId") Long employeeId
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
            model.addAttribute("listOfGovernmentIssuedId", govermentIssuedIdRepository.findAll());
        model.addAttribute("governmentIssuedId", govermentIssuedId);

        return "employee/pds/gov-issued-id";
		}

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/government-issuedId/"+employeeId;
	}

}
