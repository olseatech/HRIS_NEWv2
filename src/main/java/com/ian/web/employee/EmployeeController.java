package com.ian.web.employee;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class EmployeeController {
	
	private final EmployeeRepository employeeRepository;		
	
	@GetMapping("/employee/datalist")
    public ResponseEntity<FlexDatalistResult> doctorsFlexDatalist() {
        Set<Employee> allEmployeeSet = new HashSet<>(employeeRepository.findAll());
        return ResponseEntity.ok().body(new FlexDatalistResult(allEmployeeSet));
    }
	
	@GetMapping("/employee-list")
	public String listAll(Model model) {
		Iterable<Employee> employeeList = employeeRepository.findAll();
		model.addAttribute("employeeList", employeeList);
		model.addAttribute("employee", new Employee());
		return "employee/employee-list";
	}
	
	@GetMapping("/clearance-list")
	public String getAllClearance(Model model) {
		Iterable<Employee> employeeList = employeeRepository.findAll();
		model.addAttribute("employeeList", employeeList);
		model.addAttribute("employee", new Employee());
		return "employee/clearance/clearance-list";
	}
	
	@GetMapping("/employee/{employeeId}")
	public String viewEmployee(Model model, @PathVariable long employeeId) {
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		model.addAttribute("employee", employee);
		return "employee/pds/personnal-info";
	}
	
	@GetMapping("/employee/{employeeId}/{empHashCode}")
	public String viewEmployee(Model model, @PathVariable long employeeId, @PathVariable String empHashCode) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		Employee employee = optional.orElseGet(() -> new Employee());
		model.addAttribute("employee", employee);
		return "employee/employee-profile";
	}
	
}
