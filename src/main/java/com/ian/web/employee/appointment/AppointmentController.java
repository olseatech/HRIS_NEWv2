package com.ian.web.employee.appointment;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.systemsettings.division.DivisionRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.position_title.PositionTitleRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AppointmentController {
	
	private final EmployeeRepository employeeRepository;
	private final AppointmentRepository appointmentRepository;
	private final PositionTitleRepository positionTitleRepository;
	private final EmployeeStatusRepository employeeStatusRepository;
	private final DivisionRepository divisionRepository;
	
	
	
	@GetMapping("/appointments/{employeeId}/{empHashCode}")
	public String viewEmployeeAppointments(Model model, @PathVariable long employeeId, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();
		if(optional.isPresent()) {			
			
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}
		
		Employee employee = optional.orElseGet(() -> new Employee());
		
		model.addAttribute("employee", employee);
		
		Appointment appointment = new Appointment();
		appointment.setEmployee(employee);	
		
		model.addAttribute("appointment", appointment);
		model.addAttribute("employeeStatusList", employeeStatusRepository.findAll());
		model.addAttribute("positionTitleList", positionTitleRepository.findAll());
		
		List<Appointment> appointmentRecordList = appointmentRepository.findByEmployeeId(employeeId);
		model.addAttribute("appointmentRecordList", appointmentRecordList);
		
		if (request.getServletPath().startsWith("/profile")) {
			model.addAttribute("showMode", "PROFILE");
		} else {
			model.addAttribute("showMode", "HRADMIN");
		}		
		
		
		return "employee/appointments/employee-appointment-record";
		
	}

}
