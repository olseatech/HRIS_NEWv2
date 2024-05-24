package com.ian.web.employee.appointment;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ian.web.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AppointmentController {
	
	private final EmployeeRepository employeeRepository;
	private final AppointmentRepository appointmentRepository;
	
	@GetMapping("/appointments")
	public String viewAppointments(Model model, HttpServletRequest request) {
		
		
		return "employee/appointment/appointments";
		
	}

}
