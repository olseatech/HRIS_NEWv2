package com.ian.web.employee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ian.web.employee.clearance.Clearance;
import com.ian.web.employee.clearance.ClearanceRepository;
import com.ian.web.employee.familybg.FamilyBg;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RestApiController {
	
	Logger logger = LoggerFactory.getLogger(RestApiController.class);
	private final EmployeeRepository employeeRepository;	
	private final EmployeeStatusRepository employeeStatusRepository;
	
	private final FamilyBgRepository familyBgRepository;
	private final ClearanceRepository clearanceRepository;
	
	@GetMapping("/api/{employeeId}/{empHashCode}")
    public ResponseEntity<Employee> getEmployeeInfo(@PathVariable long employeeId, @PathVariable String empHashCode) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
				
		Employee employee = optional.orElseGet(() -> new Employee());
		
        return ResponseEntity.ok(employee);
    }
	
	@GetMapping("/employee-status/count")
    public ResponseEntity<Map<String, Long>> getEmployeeStatusCounts() {
		List<EmployeeStatus> employeeStatusList = employeeStatusRepository.findAll();
		
		Map<String, Long> statusCounts = new HashMap<>();
		Map<Long, Long> result = employeeRepository.getCountEmployeeStatus();
        for (EmployeeStatus es : employeeStatusList) {
            statusCounts.put(es.getEmployeeStatusName(), result.get(es.getId()) != null ? result.get(es.getId()) : 0L);
        }
        return ResponseEntity.ok(statusCounts);
    }
	
	@GetMapping("/clearance-list/{status}")
    public ResponseEntity<List<Clearance>> getClearanceListByStatus(@PathVariable String status) {
		List<Clearance> list = clearanceRepository.findByStatus(status);		
        return ResponseEntity.ok(list);
    }
	
	@GetMapping("/pdslink/count")
    public ResponseEntity<PdsCountDto> getPdsCountDto() {
		List<FamilyBg> familyBgList = familyBgRepository.findByEmployeeId(0);
		
		PdsCountDto dto = new PdsCountDto();
		dto.setFamilyBgCount(familyBgList.size());
		
        return ResponseEntity.ok(dto);
    }
	
	@PostMapping("/process-clearance/{id}/{status}")
    public ResponseEntity<String> approveClearance(@PathVariable Long id, @PathVariable String status, HttpSession session) {
		
		Employee loggedInUser = (Employee) session.getAttribute("actorObj");
		
		Optional<Clearance> optional =  clearanceRepository.findById(id);
		Clearance clearance = optional.orElseGet(() -> new Clearance());
		clearance.setStatus(status);
		clearance.setApprovedBy(loggedInUser.getFullName());
		clearanceRepository.save(clearance);
        
		return ResponseEntity.ok("Clearance successfully updated.");
    }
	
	@PostMapping("/api/change-credentials/{id}/{userType}/{username}/{password}")
    public ResponseEntity<String> changeCredentials(@PathVariable Long id, @PathVariable String userType, @PathVariable String username, @PathVariable String password) {
		Optional<Employee> optional = employeeRepository.findById(id);
		
		Employee employee = optional.orElseGet(() -> new Employee());
		employee.setUserType(userType);
		employee.setUsername(username);
		
		if(password.length() > 0) {
			employee.setPassword(password);
		}
		
		employeeRepository.save(employee);
        
		return ResponseEntity.ok("Credential successfully updated.");
    }
	
	

}
