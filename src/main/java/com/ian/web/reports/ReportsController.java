package com.ian.web.reports;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.clearance.Clearance;
import com.ian.web.employee.clearance.ClearanceRepository;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.eligibility.CivilServiceEligibilityRepository;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.servicerecord.ServiceRecord;
import com.ian.web.employee.servicerecord.ServiceRecordReportDto;
import com.ian.web.employee.servicerecord.ServiceRecordRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperienceRepository;
import com.ian.web.systemsettings.division.DivisionRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.position_title.PositionTitleRepository;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Controller
@RequiredArgsConstructor
public class ReportsController {
	
	public static final String HEADER_REPORT_NAME = "Sangguniang Panlungsod ng Maynila";
	public static final String EMR_RX_LOGO_URL = "/resources/static/images/rx.jpg";
	
	private final EmployeeRepository employeeRepository;	
	private final PositionTitleRepository positionTitleRepository;
	private final EmployeeStatusRepository employeeStatusRepository;
	private final DivisionRepository divisionRepository;
	
	private final FamilyBgRepository familyBgRepository;
	private final EducationalBackgroundRepository educationalBackgroundRepository;
	private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final WorkExperienceRepository workExperienceRepository;
	private final VoluntaryWorkRepository voluntaryWorkRepository;
	private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
	private final OtherInfoRepository otherInfoRepository;
//	private final OtherInfo
	private final EmpReferencesRepository empReferencesRepository;
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;
	private final ClearanceRepository clearanceRepository;
	private final ServiceRecordRepository serviceRecordRepository;
	
	@GetMapping("/viewPds/{employeeId}")
	public void viewPds(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
	}
	
	@GetMapping("/viewEmployeeListReport")
	public void viewEmployeeListReport(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
	}
	
	
	@GetMapping("/viewServiceRecordReport/{employeeId}")
	public void viewServiceRecordReport(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		List<ServiceRecord> list = serviceRecordRepository.findByEmployeeId(employeeId);
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		
		List<ServiceRecordReportDto> reportList = new ArrayList<>(); 
		
		for(ServiceRecord sr : list) {
			reportList.add(convertToDto(sr));
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Surname", employee.getLastName());
		map.put("Given_Name", employee.getFirstName());	
		map.put("Middle_Name", employee.getMiddleName());	
		map.put("BirthDate", formatDate(employee.getBirthdate()));	
		map.put("BirthPlace", employee.getBirthPlace());	
		map.put("Purpose", "For Cooperative Membership");	
		map.put("Officer", "RIZALINO A. ABUSMAN");	
		map.put("OfficerPosition", "Administrative Officer V");	
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		
		response.setContentType("application/pdf");
		
		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/Service-Record-Form.jasper");
		
		
		if(reportStream == null){
			System.out.println("reportStream is NULL");
		}
		
		if(response.getOutputStream() == null){
			System.out.println("response.getOutputStream() is NULL");
		}
		
		JasperRunManager.runReportToPdfStream(reportStream,	response.getOutputStream(), map, beanColDataSource);
		
	}
	
	@GetMapping("/viewClearanceForm/{id}")
	public void viewClearanceReport(Model model, @PathVariable long id, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
				
		Optional<Clearance> oClearance = clearanceRepository.findById(id);
		Clearance clearance = oClearance.orElseGet(() -> new Clearance());
				
		Map<String, Object> map = new HashMap<String, Object>();
		
		String empName = clearance.getEmployee().getFirstName() + " " + clearance.getEmployee().getMiddleName().charAt(0) + ". " + clearance.getEmployee().getLastName();
		
				
		map.put("City_Gov", HEADER_REPORT_NAME);
		map.put("To_City_Gov", HEADER_REPORT_NAME);
		map.put("Date1", formatDate(clearance.getTransDate()));		
		map.put("Date_Effect", formatDate(clearance.getEffectiveDate()));		
		
		map.put("Office_Of_Assignment", clearance.getEmployee().getDivision().getDivisionName());
		map.put("Position_SG_Step", clearance.getEmployee().getPositionTitle().getPositionTitleName());
		map.put("Name_ Signature_Employee", empName);
		map.put("Immediate_Supervisor", "");
		map.put("1A_Cleared", "");
		
		if("TRANSFER".equalsIgnoreCase(clearance.getPurpose())) {
			map.put("Transfer", "X");
			map.put("Retirement", "");
			map.put("Resignation", "");
			map.put("Leave", "");
			map.put("Other_methods", "");
			map.put("Specify", "");
		} else if("RETIREMENT".equalsIgnoreCase(clearance.getPurpose())) {
			map.put("Transfer", "");
			map.put("Retirement", "X");
			map.put("Resignation", "");
			map.put("Leave", "");
			map.put("Other_methods", "");
			map.put("Specify", "");
		} else if("RESIGNATION".equalsIgnoreCase(clearance.getPurpose())) {
			map.put("Transfer", "");
			map.put("Retirement", "");
			map.put("Resignation", "X");
			map.put("Leave", "");
			map.put("Other_methods", "");
			map.put("Specify", "");
		} else if("LEAVE".equalsIgnoreCase(clearance.getPurpose())) {
			map.put("Transfer", "");
			map.put("Retirement", "");
			map.put("Resignation", "");
			map.put("Leave", "X");
			map.put("Other_methods", "");
			map.put("Specify", "");
		} else {
			map.put("Transfer", "");
			map.put("Retirement", "");
			map.put("Resignation", "");
			map.put("Leave", "");
			map.put("Other_methods", "X");
			map.put("Specify", clearance.getOtherPurpose());
		}
		
		
		map.put("Cleared", "");
		map.put("Not_Cleared", "");
		map.put("IV_CONPAC_With_Pending", "");
		map.put("IV_CONPAC_With_Ongoing", "");
		
		String clearingOfficer1A = "RIZALINO A. ABUSMAN";
		String clearingOfficer1B = "ROSALINDA C. MANOJO";
		String clearingOfficer1C = " ";
		String clearingOfficer2A = "HECTOR R. PASCUAL";
		String clearingOfficer2B = "";
		String clearingOfficer3A = "";
		String clearingOfficer3B = "";
		String clearingOfficer3C = "IMELDA R. GONZALES";
		String clearingOfficer4A = "ROSALINDA C. MANOJO";
		String clearingOfficer5A = "CHARITO A. RUMBO";
		
		//1
		if(clearingOfficer1A != null && clearingOfficer1A.length() > 0) {
			map.put("1A_Name_Clearing_Officer", clearingOfficer1A + "\nAdministrative Officer V");
			map.put("1A_Cleared", "");
			map.put("1A_Not_Cleared", "");
			map.put("1A_Signature", "");
		} else {
			map.put("1A_Name_Clearing_Officer", "N/A");
			map.put("1A_Cleared", "N/A");
			map.put("1A_Not_Cleared", "N/A");
			map.put("1A_Signature", "N/A");
		}
		
		if(clearingOfficer1B != null && clearingOfficer1B.length() > 0) {
			map.put("1B_Name_Clearing_Officer", clearingOfficer1B + "\nAdministrative Officer V");
			map.put("1B_Cleared", "");
			map.put("1B_Not_Cleared", "");
			map.put("1B_Signature", "");
		} else {
			map.put("1B_Name_Clearing_Officer", "N/A");
			map.put("1B_Cleared", "N/A");
			map.put("1B_Not_Cleared", "N/A");
			map.put("1B_Signature", "N/A");
		}
		
		if(clearingOfficer1C != null && clearingOfficer1C.length() > 0) {
			map.put("1C_Name_Clearing_Officer", clearingOfficer1C + "\nAdministrative Officer V");
			map.put("1C_Cleared", "");
			map.put("1C_Not_Cleared", "");
			map.put("1C_Signature", "");
		} else {
			map.put("1C_Name_Clearing_Officer", "N/A");
			map.put("1C_Cleared", "N/A");
			map.put("1C_Not_Cleared", "N/A");
			map.put("1C_Signature", "N/A");
		}
		
		//2
		if(clearingOfficer2A != null && clearingOfficer2A.length() > 0) {
			map.put("2A_Name_Clearing_Officer", clearingOfficer2A + "\nAdministrative Officer V");
			map.put("2A_Cleared", "");
			map.put("2A_Not_Cleared", "");
			map.put("2A_Signature", "");
		} else {
			map.put("2A_Name_Clearing_Officer", "N/A");
			map.put("2A_Cleared", "N/A");
			map.put("2A_Not_Cleared", "N/A");
			map.put("2A_Signature", "N/A");
		}
		
		if(clearingOfficer2B != null && clearingOfficer2B.length() > 0) {
			map.put("2B_Name_Clearing_Officer", clearingOfficer2B + "\nAdministrative Officer V");
			map.put("2B_Cleared", "");
			map.put("2B_Not_Cleared", "");
			map.put("2B_Signature", "");
		} else {
			map.put("2B_Name_Clearing_Officer", "N/A");
			map.put("2B_Cleared", "N/A");
			map.put("2B_Not_Cleared", "N/A");
			map.put("2B_Signature", "N/A");
		}
		
		//3
		if(clearingOfficer3A != null && clearingOfficer3A.length() > 0) {
			map.put("3A_Name_Clearing_Officer", clearingOfficer3A + "\nAdministrative Officer V");
			map.put("3A_Cleared", "");
			map.put("3A_Not_Cleared", "");
			map.put("3A_Signature", "");
		} else {
			map.put("3A_Name_Clearing_Officer", "N/A");
			map.put("3A_Cleared", "N/A");
			map.put("3A_Not_Cleared", "N/A");
			map.put("3A_Signature", "N/A");
		}
		
		if(clearingOfficer3B != null && clearingOfficer3B.length() > 0) {
			map.put("3B_Name_Clearing_Officer", clearingOfficer3B + "\nAdministrative Officer V");
			map.put("3B_Cleared", "");
			map.put("3B_Not_Cleared", "");
			map.put("3B_Signature", "");
		} else {
			map.put("3B_Name_Clearing_Officer", "N/A");
			map.put("3B_Cleared", "N/A");
			map.put("3B_Not_Cleared", "N/A");
			map.put("3B_Signature", "N/A");
		}
		
		if(clearingOfficer3C != null && clearingOfficer3C.length() > 0) {
			map.put("3C_Name_Clearing_Officer", clearingOfficer3C + "\nAdministrative Officer V");
			map.put("3C_Cleared", "");
			map.put("3C_Not_Cleared", "");
			map.put("3C_Signature", "");
		} else {
			map.put("3C_Name_Clearing_Officer", "N/A");
			map.put("3C_Cleared", "N/A");
			map.put("3C_Not_Cleared", "N/A");
			map.put("3C_Signature", "N/A");
		}
		
		//4
		if(clearingOfficer4A != null && clearingOfficer4A.length() > 0) {
			map.put("4A_Name_Clearing_Officer", clearingOfficer4A + "\nAdministrative Officer V");
			map.put("4A_Cleared", "");
			map.put("4A_Not_Cleared", "");
			map.put("4A_Signature", "");
		} else {
			map.put("4A_Name_Clearing_Officer", "N/A");
			map.put("4A_Cleared", "N/A");
			map.put("4A_Not_Cleared", "N/A");
			map.put("4A_Signature", "N/A");
		}
		
		//5
		if(clearingOfficer5A != null && clearingOfficer5A.length() > 0) {
			map.put("IV_CONPAC_A_Name_Clearing_Officer", clearingOfficer5A + "\nChief Administrative Officer");
			map.put("IV_CONPAC_A_Cleared", "");
			map.put("IV_CONPAC_A_Not_Cleared", "");
			map.put("IV_CONPAC_A_Signature", "");
		} else {
			map.put("IV_CONPAC_A_Name_Clearing_Officer", "N/A");
			map.put("IV_CONPAC_A_Cleared", "N/A");
			map.put("IV_CONPAC_A_Not_Cleared", "N/A");
			map.put("IV_CONPAC_A_Signature", "N/A");
		}
		
		map.put("Immediate_Supervisor", "");
		map.put("V_CERTIFICATION_NAME2", "LUCH R. GEMPIS JR.");
		map.put("V_CERTIFICATION_POSITION2", "City Gov't. Dept. Head III");
		map.put("V_CERTIFICATION_OFFICE2", "Secretary to the City Council");
		
		
		
		map.put("V_CERTIFICATION_NAME1", "ROMEO N. FRANCIA");
		map.put("V_CERTIFICATION_POSITION1", "Officer-in-Charge");
		map.put("V_CERTIFICATION_OFFICE1", "Office of the Assistant Secretary");
				
		
		List<Employee> dataList = new ArrayList<Employee>();	
		
		Employee dummyData = new Employee();
		dummyData.setFirstName("test");
		
		dataList.add(dummyData);
		
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
		
		response.setContentType("application/pdf");
		
		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/Clearance-Form-CSC-Form.jasper");
		
		
		if(reportStream == null){
			System.out.println("reportStream is NULL");
		}
		
		if(response.getOutputStream() == null){
			System.out.println("response.getOutputStream() is NULL");
		}
		
		JasperRunManager.runReportToPdfStream(reportStream,	response.getOutputStream(), map, beanColDataSource);
		
	}
	
	private static String formatDate(LocalDate localDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy");
		return localDate.format(formatter);
	}
	
	private static ServiceRecordReportDto convertToDto(ServiceRecord serviceRecord) {
        ServiceRecordReportDto dto = new ServiceRecordReportDto();
        dto.setDateFrom(formatDate(serviceRecord.getDateFrom()));
        dto.setDateTo(formatDate(serviceRecord.getDateTo()));
        dto.setDesignation(serviceRecord.getDesignation());
        dto.setEmployeeStatus(serviceRecord.getEmployeeStatus().getEmployeeStatusName());
        dto.setSalary(getFormattedAmount(serviceRecord.getSalary())); // Format salary to two decimal places
        dto.setStation(serviceRecord.getStation());
        dto.setBranch(serviceRecord.getBranch());
        dto.setLvAbs(serviceRecord.getLvAbs());
        dto.setSeparationCause(serviceRecord.getSeparationCause());
        dto.setSeparationDate(formatDate(serviceRecord.getSeparationDate()));
        return dto;
    }
	
	private static String getFormattedAmount(Object obj){
		DecimalFormat decimalFormat = new DecimalFormat("#,###,###.00");
		return decimalFormat.format(obj);
	}

}
