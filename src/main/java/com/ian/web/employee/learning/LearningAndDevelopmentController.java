package com.ian.web.employee.learning;

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
import com.ian.web.systemsettings.learning_type.LearningType;
import com.ian.web.systemsettings.learning_type.LearningTypeRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LearningAndDevelopmentController {
    
    private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LearningTypeRepository learningTypeRepository;

    @GetMapping("/learning-development/{employeeId}")
    public String getRecord(Model model, @PathVariable("employeeId") Long id){
        if(!employeeRepository.findById(id).isPresent()){
            model.addAttribute("uxmessage", new UXMessage("ERROR", "Employee doesn't exist"));
			return "redirect:/dashboard";
        }
        Employee employee = employeeRepository.findById(id).get();
        model.addAttribute("employee", employee);
        model.addAttribute("listOfLearningType", learningTypeRepository.findAll());
        model.addAttribute("listOfLearningDevelopment", learningAndDevelopmentRepository.findAll());
        model.addAttribute("learningDevelopmentModel", new LearningDevelopmentModel());

        return "employee/pds/learning-development";
    }

    @PostMapping("/save-learning-development/{employeeId}")
    @Transactional
	public String getRecord(
			@Valid LearningDevelopmentModel learningDevelopmentModel,
            @PathVariable("employeeId") Long id
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
            model.addAttribute("listOfLearningDevelopment", learningAndDevelopmentRepository.findAll());
            model.addAttribute("learningDevelopmentModel", learningDevelopmentModel);
    
            return "employee/pds/learning-development";
		}
        Employee employee = employeeRepository.findById(id).get();
        
        LearningAndDevelopment learningAndDevelopment = new LearningAndDevelopment();
        BeanUtils.copyProperties(learningDevelopmentModel, learningAndDevelopment);
        learningAndDevelopment.setEmployee(employee);
        learningAndDevelopment.setLearningType(learningTypeRepository.findById(learningDevelopmentModel.getLearningTypeId()).get());
        learningAndDevelopmentRepository.save(learningAndDevelopment);
        
//        List<LearningAndDevelopment> listOfLearningDevelopment = employee.getLearningAndDevelopments();
//        listOfLearningDevelopment.add(learningAndDevelopment);
//        employee.setLearningAndDevelopments(listOfLearningDevelopment);
//        employeeRepository.save(employee);

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/learning-development/"+id;
	}
}
