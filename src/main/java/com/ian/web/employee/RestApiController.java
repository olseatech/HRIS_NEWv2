package com.ian.web.employee;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.changepassword.ChangePassword;
import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.clearance.Clearance;
import com.ian.web.employee.clearance.ClearanceRepository;
import com.ian.web.employee.docs201.Docs201;
import com.ian.web.employee.docs201.Docs201Repository;
import com.ian.web.employee.familybg.FamilyBg;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.servicerecord.ServiceRecordReportRequest;
import com.ian.web.employee.servicerecord.ServiceRecordReportRequestRepository;
import com.ian.web.systemsettings.division.Division;
import com.ian.web.systemsettings.division.DivisionRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RestApiController {
	
	Logger logger = LoggerFactory.getLogger(RestApiController.class);
	private final EmployeeRepository employeeRepository;	
	private final EmployeeStatusRepository employeeStatusRepository;
	private final DivisionRepository divisionRepository;
	
	private final FamilyBgRepository familyBgRepository;
	private final ClearanceRepository clearanceRepository;
	private final Docs201Repository docs201Repository;
	private final ServiceRecordReportRequestRepository serviceRecordReportRequestRepository;
	
	private static String formatDate(LocalDate localDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy");
		return localDate.format(formatter);
	}
	
	@GetMapping("/api/doc201/{id}")
    public ResponseEntity<Docs201> get201File(@PathVariable long id) {
		Optional<Docs201> optional = docs201Repository.findById(id);
				
		Docs201 obj = optional.orElseGet(() -> new Docs201());
		
        return ResponseEntity.ok(obj);
    }
	
	@GetMapping("/api/{employeeId}/{empHashCode}")
    public ResponseEntity<Employee> getEmployeeInfo(@PathVariable long employeeId, @PathVariable String empHashCode) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
				
		Employee employee = optional.orElseGet(() -> new Employee());
		
        return ResponseEntity.ok(employee);
    }
	
	@GetMapping("/employee-bdaylist")
    public ResponseEntity<List<String>> getEmployeeBirthdayList() {
		List<Employee> empList = employeeRepository.findEmployeesWithBirthMonth();
		List<String> empBdayList = new ArrayList<>();
        for (Employee e : empList) {
        	empBdayList.add(e.getFullName() + " - " + e.getAge() + " yrs old" + "<br><span class=\"font-weight-semibold text-warning\">" + formatDate(e.getBirthdate()) + "</span>" );
        	//Ian Alfred Orozco <br><span class="font-weight-semibold text-primary">Jan 27, 1982</span>
        }
        return ResponseEntity.ok(empBdayList);
    }
	
	@GetMapping("/employee-gender/count")
    public ResponseEntity<Map<String, Long>> getEmployeeGenderCounts() {
		List<Employee> empAll = employeeRepository.findAll();
		
		Map<String, Long> genderCounts = new HashMap<>();
		long male = 0;
		long female = 0;
		long lgbtq = 0;
		
		for (Employee emp : empAll) {
            if("M".equalsIgnoreCase(emp.getGender())) {
            	male++;
            } else if("F".equalsIgnoreCase(emp.getGender())) {
            	female++;
            } else {
            	lgbtq++;
            }
        }
		
		genderCounts.put("LGBTQ", lgbtq);
		genderCounts.put("MALE", male);
		genderCounts.put("FEMALE", female);		
		
        return ResponseEntity.ok(genderCounts);
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
	
	@GetMapping("/employee-division/count")
    public ResponseEntity<Map<String, Long>> getEmployeeCountByDivision() {
		List<Division> divisionList = divisionRepository.findAll();
		
		Map<String, Long> counts = new HashMap<>();
		Map<Long, Long> result = employeeRepository.getCountEmployeeDivision();
        for (Division division : divisionList) {
            counts.put(division.getDivisionName(), result.get(division.getId()) != null ? result.get(division.getId()) : 0L);
        }
        return ResponseEntity.ok(counts);
    }
	
	@GetMapping("/clearance-list/{status}")
    public ResponseEntity<List<Clearance>> getClearanceListByStatus(@PathVariable String status) {
		List<Clearance> list = clearanceRepository.findByStatus(status);		
        return ResponseEntity.ok(list);
    }
	
	@GetMapping("/clearance-list-employee/{employeeId}")
    public ResponseEntity<List<Clearance>> getClearanceListByEmployee(@PathVariable Long employeeId) {
		List<Clearance> list = clearanceRepository.findByEmployeeId(employeeId);	
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
		
		if("isHklfn35Rgnd456556rfgngdfg12".equalsIgnoreCase(password)) {
			//employee.setPassword(password);
		} else {
			employee.setPassword(password);
		}
		
		employeeRepository.save(employee);
        
		return ResponseEntity.ok("Credential successfully updated.");
    }
	
	@PostMapping("/saveServiceRecordRequest")
	public Map<String, Long> saveRecord(@RequestBody Map<String, String> request) {
        String employeeId = request.get("employeeId");
        String notes = request.get("notes");
        String printDate = request.get("printDate");
        
        ServiceRecordReportRequest obj = new ServiceRecordReportRequest();
        
        Employee emp = new Employee();
        emp.setId(Long.parseLong(employeeId));
        
        LocalDate printDateLd = LocalDate.parse((String) request.get("printDate"));
        obj.setEmployee(emp);
        obj.setNotes(notes);
        obj.setPrintDate(printDateLd);

        // Save the employeeId and notes, and generate the recordId
        obj = serviceRecordReportRequestRepository.save(obj);
        
        long recordId = obj.getId();

        // Return the recordId in the response
        Map<String, Long> response = new HashMap<>();
        response.put("recordId", recordId);
        return response;
    }

}
