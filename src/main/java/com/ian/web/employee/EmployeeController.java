package com.ian.web.employee;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
public class EmployeeController {
	
	private EmployeeRepository employeeRepository;
	
	public EmployeeController (EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}
	
	@GetMapping("/employees")
	public String listAll(Model model) {
		Iterable<Employee> employees = employeeRepository.findAll();
		model.addAttribute("employees", employees);
		return "employee/employee_list";
	}
	
	@GetMapping("/employee/{employeeId}")
	public String viewEmployee(Model model, @PathVariable long employeeId) {
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		model.addAttribute("employee", employee);
		return "employee/employee_profile";
	}
	
	@GetMapping("/employee/{employeeId}/{empHashCode}")
	public String viewEmployee(Model model, @PathVariable long employeeId, @PathVariable String empHashCode) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		Employee employee = optional.orElseGet(() -> new Employee());
		model.addAttribute("employee", employee);
		return "employee/employee_profile";
	}
	
}
