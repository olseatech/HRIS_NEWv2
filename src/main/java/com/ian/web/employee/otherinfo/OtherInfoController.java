package com.ian.web.employee.otherinfo;

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
import com.ian.web.employee.eligibility.CivilServiceEligibility;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OtherInfoController {

    private final OtherInfoRepository otherInfoRepository;

    @GetMapping("/other-info/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
        model.addAttribute("listOfOtherInfo", otherInfoRepository.findAll());
        model.addAttribute("otherInfo", new OtherInfo());

        return "employee/pds/other-info";
    }

    @PostMapping("/save-other-info/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid OtherInfo otherInfo,
            @PathVariable("employeeId") Long employeeId
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
			model.addAttribute("listOfOtherInfo", otherInfoRepository.findAll());
            model.addAttribute("otherInfo", new OtherInfo());

            return "employee/pds/other-info";
		}

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/other-info/"+employeeId;
	}
}
