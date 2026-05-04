package com.ian.web.employee;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ian.web.employee.clearance.Clearance;
import com.ian.web.employee.clearance.ClearanceRepository;
import com.ian.web.employee.docs201.Docs201;
import com.ian.web.employee.docs201.Docs201Repository;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.familybg.FamilyBg;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.servicerecord.ServiceRecordReportRequest;
import com.ian.web.employee.servicerecord.ServiceRecordReportRequestRepository;
import com.ian.web.employee.servicerecord.ServiceRecordRepository;
import com.ian.web.systemsettings.degree_courses.DegreeCoursesRepository;
import com.ian.web.systemsettings.degreelevels.DegreeLevelRepository;
import com.ian.web.systemsettings.district.DistrictRepository;
import com.ian.web.systemsettings.division.Division;
import com.ian.web.systemsettings.division.DivisionRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.position_title.PositionTitleRepository;
import com.ian.web.systemsettings.schools.SchoolRepository;

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
	
	private final DistrictRepository districtRepository;
	private final PositionTitleRepository positionTitleRepository;
	
	
	private final DegreeLevelRepository degreeLevelRepository;
	private final DegreeCoursesRepository degreeCoursesRepository;
	private final SchoolRepository schoolRepository;
	private final EducationalBackgroundRepository educationalBackgroundRepository;
	private final ServiceRecordRepository serviceRecordRepository;
	
	private static String formatDate(LocalDate localDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy");
		return localDate.format(formatter);
	}
	
	private static String generateAlphanumericHash() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            char c = uuid.charAt(i);
            if (Character.isDigit(c) || Character.isLetter(c)) {
                sb.append(c);
            } else {
                sb.append((char) ('0' + (c % 10)));
            }
        }
        return sb.toString();
    }
	
//	private static List<EmpTemplate> readCsv(String fileName) {
//        try (FileReader reader = new FileReader(fileName)) {
//            HeaderColumnNameMappingStrategy<EmpTemplate> strategy = new HeaderColumnNameMappingStrategy<>();
//            strategy.setType(EmpTemplate.class);
//
//            CsvToBean<EmpTemplate> csvToBean = new CsvToBeanBuilder<EmpTemplate>(reader)
//                    .withMappingStrategy(strategy)
//                    .withIgnoreLeadingWhiteSpace(true)
//                    .build();
//
//            return csvToBean.parse();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
	
//	private static List<EmpEduBgTemplate> readCsvEduBg(String fileName) {
//        try (FileReader reader = new FileReader(fileName)) {
//            HeaderColumnNameMappingStrategy<EmpEduBgTemplate> strategy = new HeaderColumnNameMappingStrategy<>();
//            strategy.setType(EmpEduBgTemplate.class);
//
//            CsvToBean<EmpEduBgTemplate> csvToBean = new CsvToBeanBuilder<EmpEduBgTemplate>(reader)
//                    .withMappingStrategy(strategy)
//                    .withIgnoreLeadingWhiteSpace(true)
//                    .build();
//
//            return csvToBean.parse();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//	
//	private static List<EmpServiceRecordTemplate> readCsvServiceRecord(String fileName) {
//        try (FileReader reader = new FileReader(fileName)) {
//            HeaderColumnNameMappingStrategy<EmpServiceRecordTemplate> strategy = new HeaderColumnNameMappingStrategy<>();
//            strategy.setType(EmpServiceRecordTemplate.class);
//
//            CsvToBean<EmpServiceRecordTemplate> csvToBean = new CsvToBeanBuilder<EmpServiceRecordTemplate>(reader)
//                    .withMappingStrategy(strategy)
//                    .withIgnoreLeadingWhiteSpace(true)
//                    .build();
//
//            return csvToBean.parse();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
	
//	@GetMapping("/executeDataMigration")
//	public void executeDataMigration() {
//		String fileName = "c:/temp/new-emp-manila.csv";
//		
//		List<EmpTemplate> records = readCsv(fileName);
//		int recordCount = 1;
//		
//		if (records != null) {
//	            for (EmpTemplate record : records) {
//	            	Employee employee = EmpTemplateMapper.INSTANCE.toEmpTemplate2(record);
//	            	employee.setUserType("ROLE_EMPLOYEE");
//	            	employee.setEmpHashCode(generateAlphanumericHash());
//	            	
//	            	String divisionName = employee.getDivision().getDivisionName();
//	                
//	                if(divisionName != null) {
//	        	        Division division = divisionRepository.findByDivisionName(divisionName)
//	        	                .orElseGet(() -> {
//	        	                    // If division doesn't exist, create a new one
//	        	                    Division newDivision = new Division();
//	        	                    newDivision.setDivisionName(divisionName);                    
//	        	                    return divisionRepository.save(newDivision); // Save and return
//	        	                });
//	        	        
//	        	        employee.setDivision(division);
//	                }
//	                
//	                String districtName = employee.getDistrict().getDistrictName();
//	                
//	                if(districtName != null) {
//	        	        District district = districtRepository.findByDistrictName(districtName)
//	        	                .orElseGet(() -> {
//	        	                    // If district doesn't exist, create a new one
//	        	                	District newDistrict = new District();
//	        	                	newDistrict.setDistrictName(districtName);                    
//	        	                    return districtRepository.save(newDistrict); // Save and return
//	        	                });
//	        	        
//	        	        employee.setDistrict(district);
//	                }
//	                
//	                String employeeStatusName = employee.getEmployeeStatus().getEmployeeStatusName();
//	                
//	                if(employeeStatusName != null) {
//	        	        EmployeeStatus employeeStatus = employeeStatusRepository.findByEmployeeStatusName(employeeStatusName)
//	        	                .orElseGet(() -> {
//	        	                    // If employeeStatus doesn't exist, create a new one
//	        	                	EmployeeStatus newEmployeeStatus = new EmployeeStatus();
//	        	                	newEmployeeStatus.setEmployeeStatusName(employeeStatusName);                    
//	        	                    return employeeStatusRepository.save(newEmployeeStatus); // Save and return
//	        	                });
//	        	                
//	        	        employee.setEmployeeStatus(employeeStatus);
//	                }
//	                
//	                String positionTitleName = employee.getPositionTitle().getPositionTitleName();
//	                
//	                if(positionTitleName != null) {
//	        	        PositionTitle positionTitle = positionTitleRepository.findByPositionTitleName(positionTitleName)
//	        	                .orElseGet(() -> {
//	        	                    // If positionTitle doesn't exist, create a new one
//	        	                	PositionTitle newPositionTitle = new PositionTitle();
//	        	                	newPositionTitle.setPositionTitleName(positionTitleName.toUpperCase());                    
//	        	                    return positionTitleRepository.save(newPositionTitle); // Save and return
//	        	                });
//	        	                
//	        	        employee.setPositionTitle(positionTitle);
//	                }
//	                
//	                System.out.println(recordCount + ".) Saving Emp: " + employee);
//
//	                // Persist the employee
//	                employeeRepository.save(employee);
//	            }
//	            
//	            System.out.println("----------------------------------------- DONE EMP MIGRATION -----------------------------------------");
//	            
//	            executeEduBgMigration();
//	            executeServiceRecordMigration();
//	     }
//		
//		
//	}
//	
//	@GetMapping("/executeEduBgMigration")
//	public void executeEduBgMigration() {
//		String fileName = "c:/temp/emp-edu-bg.csv";
//		
//		List<EmpEduBgTemplate> records = readCsvEduBg(fileName);
//		int recordCount = 1;
//		
//		if (records != null) {
//	            for (EmpEduBgTemplate record : records) {
//	            	
//	            	List<Employee> employees = employeeRepository.findByEmpNoOrPlantillaNo(record.getEmpNo(), record.getPlantillaNo());
//	            	System.out.println("----------- Searching Emp No: " + record.getEmpNo() + " - Plantilla: " + record.getPlantillaNo());
//	                if (!employees.isEmpty()) {
//	                	Optional<Employee> employeeOpt = employees.stream().findFirst();
//	                    Employee employee = employeeOpt.get();
//	                    System.out.println("----------- Found Record For: " + employee.getFullName());
//	                	
//	                    EducationalBackground empEduBg = EmpEduBgTemplateMapper.INSTANCE.toEmpEduBgTemplate2(record);
//	                    empEduBg.setEmployee(employee);
//		            	
//		            	String degreeName = empEduBg.getDegreeLevel().getDegreeName();
//		                
//		                if(degreeName != null) {
//		                	DegreeLevel degreeLevel = degreeLevelRepository.findByDegreeName(degreeName)
//		        	                .orElseGet(() -> {
//		        	                    // If division doesn't exist, create a new one
//		        	                	DegreeLevel newDegreeLevel = new DegreeLevel();
//		        	                    newDegreeLevel.setDegreeName(degreeName);                    
//		        	                    return degreeLevelRepository.save(newDegreeLevel); // Save and return
//		        	                });
//		        	        
//		        	        empEduBg.setDegreeLevel(degreeLevel);
//		                }
//		                
//		                String degreeCourseName = empEduBg.getDegreeCourse().getDegreeCourseName();
//		                
//		                if(degreeCourseName != null) {
//		        	        DegreeCourses degreeCourse = degreeCoursesRepository.findByDegreeCourseName(degreeCourseName.toUpperCase())
//		        	                .orElseGet(() -> {
//		        	                    // If district doesn't exist, create a new one
//		        	                	DegreeCourses newDegreeCourse = new DegreeCourses();
//		        	                	newDegreeCourse.setDegreeCourseName(degreeCourseName.toUpperCase());                    
//		        	                    return degreeCoursesRepository.save(newDegreeCourse); // Save and return
//		        	                });
//		        	        
//		        	        empEduBg.setDegreeCourse(degreeCourse);
//		                
//		                
//			                String schoolName = empEduBg.getSchool().getSchoolName();
//			                
//			                if(schoolName != null) {
//			        	        School school = schoolRepository.findBySchoolName(schoolName)
//			        	                .orElseGet(() -> {
//			        	                    // If employeeStatus doesn't exist, create a new one
//			        	                	School newSchool = new School();
//			        	                	newSchool.setSchoolName(schoolName);                    
//			        	                    return schoolRepository.save(newSchool); // Save and return
//			        	                });
//			        	                
//			        	        empEduBg.setSchool(school);
//			                }
//			                	                
//			                System.out.println(recordCount + ".) Saving Emp Educ Bg: " + empEduBg);
//			                
//			                // Now check if this degree course is already associated with the employee
//			                Optional<EducationalBackground> existingEducationalBackground = educationalBackgroundRepository.findByEmployeeAndDegreeCourse(employee, degreeCourse);
//
//			                if (!existingEducationalBackground.isPresent()) {
//			                    // Degree course does not exist for this employee, so save it
//			                    educationalBackgroundRepository.save(empEduBg);
//			                    System.out.println("EducationalBackground saved for employee: " + employee.getFullName());
//			                } else {
//			                    // Degree course already exists for this employee
//			                    System.out.println("This degree course is already present for the employee: " + employee.getFullName());
//			                }
//			                
//			                // Persist the employee
//			                //educationalBackgroundRepository.save(empEduBg);
//		                 }
//	                }
//	            	
//	            	
//	            }
//	            
//	            System.out.println("----------------------------------------- DONE EDU BG MIGRATION -----------------------------------------");
//	     }
//		
//		
//	}
//	
//	@GetMapping("/executeServiceRecordMigration")
//	public void executeServiceRecordMigration() {
//		String fileName = "c:/temp/emp_service-record-manila.csv";
//		
//		List<EmpServiceRecordTemplate> records = readCsvServiceRecord(fileName);
//		int recordCount = 1;
//		
//		if (records != null) {
//	            for (EmpServiceRecordTemplate record : records) {
//	            	
//	            	List<Employee> employees = employeeRepository.findByEmpNoOrPlantillaNo(record.getEmpNo(), record.getPlantillaNo());
//	            	System.out.println("----------- Searching Emp No: " + record.getEmpNo() + " - Plantilla: " + record.getPlantillaNo());
//	                if (!employees.isEmpty()) {
//	                	Optional<Employee> employeeOpt = employees.stream().findFirst();
//	                    Employee employee = employeeOpt.get();
//	                    System.out.println("----------- Found Record For: " + employee.getFullName());
//	                	
//	                    ServiceRecord empServiceRecord = EmpServiceRecordTemplateMapper.INSTANCE.toEmpServiceRecordTemplate2(record);
//	                    empServiceRecord.setEmployee(employee);
//	                    
//	                    String employeeStatusName = empServiceRecord.getEmployeeStatus().getEmployeeStatusName();
//		                
//		                if(employeeStatusName != null) {
//		        	        EmployeeStatus employeeStatus = employeeStatusRepository.findByEmployeeStatusName(employeeStatusName)
//		        	                .orElseGet(() -> {
//		        	                    // If employeeStatus doesn't exist, create a new one
//		        	                	EmployeeStatus newEmployeeStatus = new EmployeeStatus();
//		        	                	newEmployeeStatus.setEmployeeStatusName(employeeStatusName);                    
//		        	                    return employeeStatusRepository.save(newEmployeeStatus); // Save and return
//		        	                });
//		        	                
//		        	        empServiceRecord.setEmployeeStatus(employeeStatus);
//		                }
//		                
//		                String positionTitleName = empServiceRecord.getPositionTitle().getPositionTitleName();
//		                
//		                if(positionTitleName != null) {
//		        	        PositionTitle positionTitle = positionTitleRepository.findByPositionTitleName(positionTitleName)
//		        	                .orElseGet(() -> {
//		        	                    // If positionTitle doesn't exist, create a new one
//		        	                	PositionTitle newPositionTitle = new PositionTitle();
//		        	                	newPositionTitle.setPositionTitleName(positionTitleName.toUpperCase());                    
//		        	                    return positionTitleRepository.save(newPositionTitle); // Save and return
//		        	                });
//		        	                
//		        	        empServiceRecord.setPositionTitle(positionTitle);
//		                }
//	                    
//	                    
//		                // Persist the employee
//		                serviceRecordRepository.save(empServiceRecord);            	
//		            	
//	                }
//	            	
//	            	
//	            }
//	            
//	            System.out.println("----------------------------------------- DONE SERVICE RECORD MIGRATION -----------------------------------------");
//	     }
//		
//		
//	}
	
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
