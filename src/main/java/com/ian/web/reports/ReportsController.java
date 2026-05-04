package com.ian.web.reports;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.PdsCountDto;
import com.ian.web.employee.approvers.ClearanceApprovers;
import com.ian.web.employee.approvers.ClearanceApproversRepository;
import com.ian.web.employee.approvers.ServiceRecordSignatory;
import com.ian.web.employee.approvers.ServiceRecordSignatoryRepository;
import com.ian.web.employee.clearance.Clearance;
import com.ian.web.employee.clearance.ClearanceRepository;
import com.ian.web.employee.educationalbg.EducationalBackground;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.eligibility.CivilServiceEligibility;
import com.ian.web.employee.eligibility.CivilServiceEligibilityRepository;
import com.ian.web.employee.familybg.FamilyBg;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.govermentid.GovermentIssuedId;
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.learning.LearningAndDevelopment;
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfo;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestion;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestionRepository;
import com.ian.web.employee.references.EmpReferences;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.servicerecord.ServiceRecord;
import com.ian.web.employee.servicerecord.ServiceRecordReportDto;
import com.ian.web.employee.servicerecord.ServiceRecordReportRequest;
import com.ian.web.employee.servicerecord.ServiceRecordReportRequestRepository;
import com.ian.web.employee.servicerecord.ServiceRecordRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWork;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperience;
import com.ian.web.employee.workexperience.WorkExperienceRepository;
import com.ian.web.systemsettings.division.DivisionRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.position_title.PositionDescriptionForm;
import com.ian.web.systemsettings.position_title.PositionDescriptionFormRepository;
import com.ian.web.systemsettings.position_title.PositionTitle;
import com.ian.web.systemsettings.position_title.PositionTitleRepository;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import java.time.Month;
import org.springframework.web.bind.annotation.ModelAttribute;

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
	private final OtherInfoQuestionRepository otherInfoQuestionRepository;
	private final EmpReferencesRepository empReferencesRepository;
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;
	private final ClearanceRepository clearanceRepository;
	private final ServiceRecordRepository serviceRecordRepository;
	private final ClearanceApproversRepository clearanceApproversRepository;
	private final ServiceRecordReportRequestRepository serviceRecordReportRequestRepository;
	private final ServiceRecordSignatoryRepository serviceRecordSignatoryRepository;
	private final PositionDescriptionFormRepository positionDescriptionFormRepository;
	
	private final ResourceLoader resourceLoader;
	
	@GetMapping("/viewRaiReport")
    public void viewRaiReport(@ModelAttribute RaiReportDto raiReportDto, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
        
//		Optional<PositionDescriptionForm> optional = positionDescriptionFormRepository.findById(1);
//		PositionDescriptionForm positionDescriptionForm = optional.orElseGet(() -> new PositionDescriptionForm());		
		
		
		
		response.setContentType("application/pdf");
		
		 // 1. Convert month name and year string to integers
	    int monthNumber = Month.valueOf(raiReportDto.getMonth().toUpperCase()).getValue();
	    int yearNumber = Integer.parseInt(raiReportDto.getYear());
		
		 // 2. Call the updated repository method
	    List<ServiceRecord> accendedEmployees = serviceRecordRepository.findByEntranceDateMonthAndYear(monthNumber, yearNumber);
	    
	    int ctr = 1;

	    List<RaiDetailDto> dataList = new ArrayList<>();
	    for (ServiceRecord record : accendedEmployees) {
	    	RaiDetailDto dto = new RaiDetailDto();

	    	dto.setD1(ctr+"");
	    	dto.setD2(formatDate(record.getEntranceDate()));
			dto.setD3(getStringValue(record.getEmployee().getLastName()));
			dto.setD4(getStringValue(record.getEmployee().getFirstName()));
			dto.setD5(getStringValue(record.getEmployee().getSuffix()));
			dto.setD6(getStringValue(record.getEmployee().getMiddleName()));
			
			if(record.getPositionTitle() != null) {
				dto.setD7(getStringValue(record.getPositionTitle().getPositionTitleName()));
			} else {
				dto.setD7("");
			}
			//Ian
			dto.setD8(getStringValue(record.getPlantillaNo()));
			dto.setD9(getStringValue(getStringValueFromInt(record.getSalaryGrade()) + "-" + getStringValueFromInt(record.getStepInc())));
//			dto.setD9(record.getSalaryGrade().toString());
			
//			Double annualSalary = record.getSalary() * 13;
			
			dto.setD10(getFormattedAmountWithoutDecimal(record.getSalary()));
			dto.setD11(record.getEmployeeStatus().getEmployeeStatusName());
			dto.setD12("N/A"); //period
			dto.setD13(record.getStatusOfAppointment());
			
			
			String datePublication = "";
			
			if (record.getPublicationDateFrom() != null && record.getPublicationDateTo() != null) {
				datePublication = formatDate(record.getPublicationDateFrom()) + "-" + formatDate(record.getPublicationDateTo());
			}
			
			dto.setD14(datePublication);
			dto.setD15(getStringValue(record.getModeOfPublication()));
			dto.setD16(getStringValue(record.getValidateInv()));
			dto.setD17(formatDate(record.getDateOfActionCscAction()));
			dto.setD18(formatDate(record.getDateOfReleaseCscAction()));
			dto.setD19(getStringValue(record.getAgencyReceivingOfficer())); 
			
			ctr++;

            dataList.add(dto);
        }
	    
	    //check if dataList is empty if yes assign dummy data to show the report by default
	    if (dataList.isEmpty()) {
	    	RaiDetailDto dummyData = new RaiDetailDto();
			dummyData.setD1("1");
			dummyData.setD2("");
			dummyData.setD3("");
			dummyData.setD4("");
			dummyData.setD5("");
			dummyData.setD6("");
			dummyData.setD7("");
			dummyData.setD8("");
			dummyData.setD9("");
			dummyData.setD10("");
			dummyData.setD11("");
			dummyData.setD12("");
			dummyData.setD13("");
			dummyData.setD14("");
			dummyData.setD15("");
			dummyData.setD16("");
			dummyData.setD17("");
			dummyData.setD18("");
			dummyData.setD19("");
			
			dataList.add(dummyData);
	    }

		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/RAI-REPORT.jasper");
		Map<String, Object> map = populateMapRsiRptP1(raiReportDto);
		
		InputStream reportStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/RAI-REPORT-PAGE2.jasper");
		Map<String, Object> map2 = populateMapRsiRptP2(raiReportDto);
		
		List<ServiceRecordReportDto> reportList = new ArrayList<>(); 
		ServiceRecordReportDto x = new ServiceRecordReportDto();
		x.setStation("test");
		
		reportList.add(x);
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
		
		JasperPrint jasperPrint1 = JasperFillManager.fillReport(reportStream, map, beanColDataSource);
		JasperPrint jasperPrint2 = JasperFillManager.fillReport(reportStream2, map2, new JREmptyDataSource());

		
		List<JRPrintPage> pages2 = jasperPrint2.getPages();
        for (JRPrintPage page : pages2) {
            jasperPrint1.addPage(page);
        }        

        JasperExportManager.exportReportToPdfStream(jasperPrint1, response.getOutputStream());

    }
	
	
	@GetMapping("/viewPositionDescForm/{id}")	
	public void viewPositionDescForm(Model model, @PathVariable long id, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
		Optional<PositionDescriptionForm> optional = positionDescriptionFormRepository.findById(id);
		PositionDescriptionForm positionDescriptionForm = optional.orElseGet(() -> new PositionDescriptionForm());		
		
		List<ServiceRecordReportDto> reportList = new ArrayList<>(); 
		ServiceRecordReportDto x = new ServiceRecordReportDto();
		x.setStation("test");
		
		reportList.add(x);
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		
		response.setContentType("application/pdf");

		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PositionDescFormPage1.jasper");
		Map<String, Object> map = populateMapPostionDescRptP1(positionDescriptionForm);
		
		InputStream reportStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PositionDescFormPage2.jasper");
		Map<String, Object> map2 = populateMapPostionDescRptP2(positionDescriptionForm);
		
		JasperPrint jasperPrint1 = JasperFillManager.fillReport(reportStream, map, new JREmptyDataSource());
		JasperPrint jasperPrint2 = JasperFillManager.fillReport(reportStream2, map2, new JREmptyDataSource());

		
		List<JRPrintPage> pages2 = jasperPrint2.getPages();
        for (JRPrintPage page : pages2) {
            jasperPrint1.addPage(page);
        }        

        JasperExportManager.exportReportToPdfStream(jasperPrint1, response.getOutputStream());

	}
	
	private Map<String, Object> populateMapPostionDescRptP2(PositionDescriptionForm obj) throws FileNotFoundException {
        Map<String, Object> map = new HashMap<>();

        // --- Setup and Background Image ---
        File file = ResourceUtils.getFile("classpath:static/images/PositionDecFormPage2_v2.jpg");
        String bgImg = file.getAbsolutePath();
        map.put("FormBg2", bgImg);
        
        // Box 20: Job Summary
        map.put("JOB_SUMMARY", getStringValue(obj.getJobSummary()));

        // Box 21: Qualification Standards
        map.put("QS_EDUCATION", getStringValue(obj.getQsEducation()));
        map.put("QS_EXPERIENCE", getStringValue(obj.getQsExperience()));
        map.put("QS_TRAINING", getStringValue(obj.getQsTraining()));
        map.put("QS_ELIGIBILITY", getStringValue(obj.getQsEligibility()));

        // Box 21e & 21f: Competencies
        map.put("CORE_COMPETENCIES", getStringValue(obj.getCoreCompetencies()));
        map.put("CORE_COMPETENCY_LEVEL", getStringValue(obj.getCoreCompetencyLevel()));
        map.put("LEADERSHIP_COMPETENCIES", getStringValue(obj.getLeadershipCompetencies()));
        map.put("LEADERSHIP_COMPETENCY_LEVEL", getStringValue(obj.getLeadershipCompetencyLevel()));

        // Box 22: Statement of Duties and Responsibilities
        map.put("DUTY_DESCRIPTION", getStringValue(obj.getDutyDescription()));
        map.put("DUTY_PERCENTAGE", getStringValue(obj.getDutyPercentage()));
        map.put("DUTY_COMPETENCY", getStringValue(obj.getDutyCompetencyLevel()));

        // Box 23: Signatures
        map.put("EMPLOYEE_NAME", getStringValue(obj.getEmployeeName()));
        map.put("SUPERVISOR_NAME", getStringValue(obj.getSupervisorName()));

        return map;
    }
	
	//PositionDecFormPage2_v2.jpg
		private Map<String, Object> populateMapPostionDescRptP2Backup(PositionDescriptionForm obj) throws FileNotFoundException {
		    Map<String, Object> map = new HashMap<>();

		    // --- Setup and Background Image ---
		    // Retrieves the background image file from the classpath.
		    File file = ResourceUtils.getFile("classpath:static/images/PositionDecFormPage2_v2.jpg");
		    String bgImg = file.getAbsolutePath();
		    map.put("FormBg2", bgImg);
		    
		 // Box 20: Job Summary
		    map.put("JOB_SUMMARY", "Provides overall management of the Human Resource functions which include recruitment, selection and placement, learning and development, performance management, and rewards and recognition in accordance with the Civil Service rules and regulations.");

		    // Box 21: Qualification Standards
		    map.put("QS_EDUCATION", "Bachelor's degree relevant to the job");
		    map.put("QS_EXPERIENCE", "2 years of relevant experience");
		    map.put("QS_TRAINING", "8 hours of relevant training");
		    map.put("QS_ELIGIBILITY", "Career Service (Professional)");

		    // Box 21e & 21f: Competencies
		    map.put("CORE_COMPETENCIES", "Exemplifying Integrity\nDelivering Service Excellence\nSolving Problems and Making Decisions");
		    map.put("CORE_COMPETENCY_LEVEL", "Advanced");
		    map.put("LEADERSHIP_COMPETENCIES", "Building Collaborative and Inclusive Working Relationships\nManaging Performance and Coaching for Results\nLeading Change");
		    map.put("LEADERSHIP_COMPETENCY_LEVEL", "Advanced");

		    // Box 22: Statement of Duties and Responsibilities (Updated from screenshot)
		    String dutyDescription = "1. Responsible for safekeeping of confidential documents and record, that require personal attention of the councilor;\n\n" +
		                             "2. Responsible for typing and encoding personal or confidential letters/correspondence of the office;\n\n" +
		                             "3. Messengerial functions;\n\n" +
		                             "4. Performs other functions as may be assigned by the councilor from time to time.";
		    String dutyPercentage = "50%\n\n25%\n\n15%\n\n10%";

		    map.put("DUTY_DESCRIPTION", dutyDescription);
		    map.put("DUTY_PERCENTAGE", dutyPercentage);
		    map.put("DUTY_COMPETENCY", "Competent");

		    // Box 23: Signatures (Assuming names are passed from another source)
		    map.put("EMPLOYEE_NAME", "JUAN C. DELA CRUZ");
		    map.put("SUPERVISOR_NAME", "MARIA S. REYES");

		    return map;
		}
		
		private Map<String, Object> populateMapPostionDescRptP1(PositionDescriptionForm obj) throws FileNotFoundException {
	        Map<String, Object> map = new HashMap<>();

	        // --- Setup and Background Image ---
	        File file = ResourceUtils.getFile("classpath:static/images/PositionDecFormPage1.jpg");
	        String bgImg = file.getAbsolutePath();
	        map.put("FormBg", bgImg);

	        // --- Populating Form Fields based on the obj parameter ---

	        // Box 1-3
	        map.put("POSITION_TITLE", obj.getPositionTitle() != null ? getStringValue(obj.getPositionTitle().getPositionTitleName().toUpperCase()) : "");
	        map.put("ITEM_NUMBER", getStringValue(obj.getItemNumber()));
	        map.put("SALARY_GRADE", getStringValue(obj.getSalaryGrade()));

	        // Box 4: Local Government Position (Using "X" for checked, "" for unchecked)
	        map.put("LGU_PROVINCE", obj.isLguProvince() ? "X" : "");
	        map.put("LGU_CITY", obj.isLguCity() ? "X" : "");
	        map.put("LGU_MUNICIPALITY", obj.isLguMunicipality() ? "X" : "");
	        map.put("LGU_CLASS_1", obj.isLguClass1() ? "X" : "");
	        map.put("LGU_CLASS_2", obj.isLguClass2() ? "X" : "");
	        map.put("LGU_CLASS_3", obj.isLguClass3() ? "X" : "");
	        map.put("LGU_CLASS_4", obj.isLguClass4() ? "X" : "");
	        map.put("LGU_CLASS_5", obj.isLguClass5() ? "X" : "");
	        map.put("LGU_CLASS_6", obj.isLguClass6() ? "X" : "");
	        map.put("LGU_CLASS_SPECIAL", obj.isLguClassSpecial() ? "X" : "");

	        // Box 5-8
	        map.put("DEPARTMENT_AGENCY", getStringValue(obj.getDepartmentAgency()));
	        map.put("BUREAU_OFFICE", getStringValue(obj.getBureauOffice()));
	        map.put("BRANCH_DIVISION", getStringValue(obj.getBranchDivision()));
	        map.put("WORKSTATION", getStringValue(obj.getWorkstation()));

	        // Box 9-12
	        map.put("PRESENT_APPROP_ACT", getStringValue(obj.getPresentAppropriationAct()));
	        map.put("PREVIOUS_APPROP_ACT", getStringValue(obj.getPreviousAppropriationAct()));
	        map.put("SALARY_AUTHORIZED", getStringValue(obj.getSalaryAuthorized()));
	        map.put("OTHER_COMPENSATION", getStringValue(obj.getOtherCompensation()));

	        // Box 13-14
	        map.put("IMMEDIATE_SUPERVISOR_TITLE", getStringValue(obj.getImmediateSupervisorTitle()));
	        map.put("NEXT_HIGHER_SUPERVISOR_TITLE", getStringValue(obj.getNextHigherSupervisorTitle()));

	        // Box 15
	        map.put("SUPERVISED_STAFF_POSITION_TITLE", getStringValue(obj.getSupervisedStaffPositionTitle()));
	        map.put("SUPERVISED_STAFF_ITEM_NUMBER", getStringValue(obj.getSupervisedStaffItemNumber()));

	        // Box 16
	        map.put("EQUIPMENT_USED", getStringValue(obj.getEquipmentUsed()));

	        // Box 17: Contacts/Clients/Stakeholders
	        map.put("CONTACT_INTERNAL_EXEC_OCCASIONAL", "Occasional".equalsIgnoreCase(getStringValue(obj.getContactInternalExec())) ? "X" : "");
	        map.put("CONTACT_INTERNAL_EXEC_FREQUENT", "Frequent".equalsIgnoreCase(getStringValue(obj.getContactInternalExec())) ? "X" : "");
	        map.put("CONTACT_INTERNAL_SUPERVISOR_OCCASIONAL", "Occasional".equalsIgnoreCase(getStringValue(obj.getContactInternalSupervisor())) ? "X" : "");
	        map.put("CONTACT_INTERNAL_SUPERVISOR_FREQUENT", "Frequent".equalsIgnoreCase(getStringValue(obj.getContactInternalSupervisor())) ? "X" : "");
	        map.put("CONTACT_INTERNAL_NON_SUPERVISOR_OCCASIONAL", "Occasional".equalsIgnoreCase(getStringValue(obj.getContactInternalNonSupervisor())) ? "X" : "");
	        map.put("CONTACT_INTERNAL_NON_SUPERVISOR_FREQUENT", "Frequent".equalsIgnoreCase(getStringValue(obj.getContactInternalNonSupervisor())) ? "X" : "");
	        map.put("CONTACT_INTERNAL_STAFF_OCCASIONAL", "Occasional".equalsIgnoreCase(getStringValue(obj.getContactInternalStaff())) ? "X" : "");
	        map.put("CONTACT_INTERNAL_STAFF_FREQUENT", "Frequent".equalsIgnoreCase(getStringValue(obj.getContactInternalStaff())) ? "X" : "");
	        map.put("CONTACT_EXTERNAL_PUBLIC_OCCASIONAL", "Occasional".equalsIgnoreCase(getStringValue(obj.getContactExternalPublic())) ? "X" : "");
	        map.put("CONTACT_EXTERNAL_PUBLIC_FREQUENT", "Frequent".equalsIgnoreCase(getStringValue(obj.getContactExternalPublic())) ? "X" : "");
	        map.put("CONTACT_EXTERNAL_AGENCIES_OCCASIONAL", "Occasional".equalsIgnoreCase(getStringValue(obj.getContactExternalAgencies())) ? "X" : "");
	        map.put("CONTACT_EXTERNAL_AGENCIES_FREQUENT", "Frequent".equalsIgnoreCase(getStringValue(obj.getContactExternalAgencies())) ? "X" : "");
	        map.put("CONTACT_EXTERNAL_OTHERS_TEXT", getStringValue(obj.getContactExternalOthersText()));

	        // Box 18: Working Condition
	        map.put("WORK_CONDITION_OFFICE_OCCASIONAL", "Occasional".equalsIgnoreCase(getStringValue(obj.getWorkConditionOffice())) ? "X" : "");
	        map.put("WORK_CONDITION_OFFICE_FREQUENT", "Frequent".equalsIgnoreCase(getStringValue(obj.getWorkConditionOffice())) ? "X" : "");
	        map.put("WORK_CONDITION_FIELD_OCCASIONAL", "Occasional".equalsIgnoreCase(getStringValue(obj.getWorkConditionField())) ? "X" : "");
	        map.put("WORK_CONDITION_FIELD_FREQUENT", "Frequent".equalsIgnoreCase(getStringValue(obj.getWorkConditionField())) ? "X" : "");
	        map.put("WORK_CONDITION_OTHERS_TEXT", getStringValue(obj.getWorkConditionOthersText()));

	        // Box 19
	        map.put("FUNCTION_OF_UNIT", getStringValue(obj.getFunctionOfUnit()));

	        return map;
	    }
		
		private Map<String, Object> populateMapRsiRptP1(RaiReportDto raiReportDto) throws FileNotFoundException {
		    Map<String, Object> map = new HashMap<>();

		    map.put("AGENCY", raiReportDto.getAgency());
		    map.put("PERIOD", raiReportDto.getMonth() + " - " + raiReportDto.getYear());
		    map.put("CSC_RES_NO", raiReportDto.getResolutionNo());
		    map.put("CSC_OFFICER", raiReportDto.getCscOfficer());
	        map.put("DATE_RECEIVED", "");
		    map.put("SIGNATORY", raiReportDto.getSignatory1());
		    map.put("SIGNATORY_POSITION", "Highest Ranking HRMO");
		    map.put("SIGNATORY2", raiReportDto.getSignatory2());
		    map.put("SIGNATORY_POSITION2", "Agency Head or Authorized Official");
		    map.put("SIGNATORY3", raiReportDto.getSignatory3());
		    map.put("SIGNATORY_POSITION3", "CSC Official");

		    return map;
		}
		
		private Map<String, Object> populateMapRsiRptP2(RaiReportDto raiReportDto) throws FileNotFoundException {
			Map<String, Object> map = new HashMap<>();

		    map.put("signatory4", raiReportDto.getSignatory1());
		    map.put("signatory5", raiReportDto.getSignatory3());
		    map.put("position4", "Highest Ranking HRMO");
		    map.put("position5", "CSC FO Receiving Officer");
		    
		    map.put("hrmoComment1", raiReportDto.getHrmoComment1());
		    map.put("hrmoComment2", raiReportDto.getHrmoComment2());
		    map.put("hrmoComment3", raiReportDto.getHrmoComment3());
		    map.put("hrmoComment4", raiReportDto.getHrmoComment4());
		    map.put("hrmoComment5", raiReportDto.getHrmoComment5());
		    map.put("hrmoComment6", raiReportDto.getHrmoComment6());
		    map.put("hrmoComment7", raiReportDto.getHrmoComment7());
		    
		    map.put("cscfoComment1", raiReportDto.getCscfoComment1());
		    map.put("cscfoComment2", raiReportDto.getCscfoComment2());
		    map.put("cscfoComment3", raiReportDto.getCscfoComment3());
		    map.put("cscfoComment4", raiReportDto.getCscfoComment4());
		    map.put("cscfoComment5", raiReportDto.getCscfoComment5());
		    map.put("cscfoComment6", raiReportDto.getCscfoComment6());
		    map.put("cscfoComment7", raiReportDto.getCscfoComment7());
		    
		    
		    return map;
		}
	

	@GetMapping("/viewReportOfAccession")
	public void viewReportOfAccession(Model model,
	                                @RequestParam String signatoryName,
	                                @RequestParam String signatoryPosition,
	                                @RequestParam String month, // e.g., "JANUARY"
	                                @RequestParam String year,  // e.g., "2025"
	                                HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {

	    // 1. Convert month name and year string to integers
	    int monthNumber = Month.valueOf(month.toUpperCase()).getValue();
	    int yearNumber = Integer.parseInt(year);

	    // 2. Call the updated repository method
	    List<ServiceRecord> accendedEmployees = serviceRecordRepository.findByEntranceDateMonthAndYear(monthNumber, yearNumber);

	    List<ReportOnAccessionSeparationDto> dataList = new ArrayList<>();
	    for (ServiceRecord record : accendedEmployees) {
            ReportOnAccessionSeparationDto dto = new ReportOnAccessionSeparationDto();

            dto.setEmpName(getStringValue(record.getEmployee().getFullName()));
            
            if (record.getPositionTitle() != null) {
                dto.setPositionTitle(getStringValue(record.getPositionTitle().getPositionTitleName()));
            } else {
            	dto.setPositionTitle("");
            }
            
            dto.setSalaryGrade(getStringValue(getStringValueFromInt(record.getSalaryGrade()) + "-" + getStringValueFromInt(record.getStepInc())));            
            dto.setLevelOfPosition(getStringValue(record.getLevelOfPosition())); 
            
            if (record.getEmployeeStatus() != null) {
                dto.setStatusOfAppointment(getStringValue(record.getEmployeeStatus().getEmployeeStatusName()));
            } else {
            	dto.setStatusOfAppointment("");
            }            
            
            if (record.getEntranceDate() != null) {
                dto.setEffectivityDate(getStringValue(record.getEntranceDate().minusDays(1).toString()));
            } else {
            	dto.setEffectivityDate("");
            }
            
            dto.setModeOfAccession(getStringValue(record.getStatusOfAppointment()));            

            dataList.add(dto);
        }
	    
	    //check if dataList is empty if yes assign dummy data to show the report by default
	    if (dataList.isEmpty()) {
	    	ReportOnAccessionSeparationDto dummyData = new ReportOnAccessionSeparationDto();
			dummyData.setEmpName("");
			dummyData.setPositionTitle("");
			dummyData.setSalaryGrade("");    
			dummyData.setLevelOfPosition(""); 
			dummyData.setStatusOfAppointment("");
			dummyData.setEffectivityDate("");
			dummyData.setModeOfAccession("");        
			
			dataList.add(dummyData);
	    }

	    // This part remains the same...
	    Map<String, Object> map = new HashMap<>();
	    map.put("OFFICE_NAME", "CITY COUNCIL, MANILA");
	    map.put("REPORT_MODE", "MODE OF ACCESSION");
	    map.put("SIGNATORY", getStringValue(signatoryName));
        map.put("SIGNATORY_POSITION", getStringValue(signatoryPosition));
	    map.put("REPORT_NAME", "REPORT ON ACCESSION");
	    map.put("PERIOD", "For the month of " + getStringValue(month) + ", " + getStringValue(year));

	    JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
	    response.setContentType("application/pdf");
	    InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jasper/reports/REPORT-ON-SEPARATION.jasper");

	    if (reportStream == null) {
	        System.out.println("Jasper report not found.");
	        // Handle error appropriately
	        return;
	    }

	    JasperRunManager.runReportToPdfStream(reportStream, response.getOutputStream(), map, beanColDataSource);
	}
	
	@GetMapping("/viewReportOfSeparation")
	public void viewRptOfSeparation(Model model,
	                                @RequestParam String signatoryName,
	                                @RequestParam String signatoryPosition,
	                                @RequestParam String month, // e.g., "JANUARY"
	                                @RequestParam String year,  // e.g., "2025"
	                                HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {

	    // 1. Convert month name and year string to integers
	    int monthNumber = Month.valueOf(month.toUpperCase()).getValue();
	    int yearNumber = Integer.parseInt(year);

	    // 2. Call the updated repository method
	    List<ServiceRecord> separatedEmployees = serviceRecordRepository.findBySeparationDateMonthAndYear(monthNumber, yearNumber);

	    List<ReportOnAccessionSeparationDto> dataList = new ArrayList<>();
	    for (ServiceRecord record : separatedEmployees) {
            ReportOnAccessionSeparationDto dto = new ReportOnAccessionSeparationDto();

            dto.setEmpName(getStringValue(record.getEmployee().getFullName()));
            
            if (record.getPositionTitle() != null) {
                dto.setPositionTitle(getStringValue(record.getPositionTitle().getPositionTitleName()));
            } else {
            	dto.setPositionTitle("");
            }
            
            dto.setSalaryGrade(getStringValue(getStringValueFromInt(record.getSalaryGrade()) + "-" + getStringValueFromInt(record.getStepInc())));            
            dto.setLevelOfPosition(getStringValue(record.getLevelOfPosition())); 
            
            if (record.getEmployeeStatus() != null) {
                dto.setStatusOfAppointment(getStringValue(record.getEmployeeStatus().getEmployeeStatusName()));
            } else {
            	dto.setStatusOfAppointment("");
            }            
            
            if (record.getSeparationDate() != null) {
                dto.setEffectivityDate(getStringValue(record.getSeparationDate().minusDays(1).toString()));
            } else {
            	dto.setEffectivityDate("");
            }
            
            dto.setModeOfAccession(getStringValue(record.getSeparationCause()));            

            dataList.add(dto);
        }
	    
	    //check if dataList is empty if yes assign dummy data to show the report by default
	    if (dataList.isEmpty()) {
	    	ReportOnAccessionSeparationDto dummyData = new ReportOnAccessionSeparationDto();
			dummyData.setEmpName("");
			dummyData.setPositionTitle("");
			dummyData.setSalaryGrade("");    
			dummyData.setLevelOfPosition(""); 
			dummyData.setStatusOfAppointment("");
			dummyData.setEffectivityDate("");
			dummyData.setModeOfAccession("");        
			
			dataList.add(dummyData);
	    }

	    // This part remains the same...
	    Map<String, Object> map = new HashMap<>();
	    map.put("OFFICE_NAME", "CITY COUNCIL, MANILA");
	    map.put("REPORT_MODE", "MODE OF SEPARATION");
	    map.put("SIGNATORY", getStringValue(signatoryName));
        map.put("SIGNATORY_POSITION", getStringValue(signatoryPosition));
	    map.put("REPORT_NAME", "REPORT ON SEPARATION");
	    map.put("PERIOD", "For the month of " + getStringValue(month) + ", " + getStringValue(year));

	    JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
	    response.setContentType("application/pdf");
	    InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jasper/reports/REPORT-ON-SEPARATION.jasper");

	    if (reportStream == null) {
	        System.out.println("Jasper report not found.");
	        // Handle error appropriately
	        return;
	    }

	    JasperRunManager.runReportToPdfStream(reportStream, response.getOutputStream(), map, beanColDataSource);
	}
	
	@GetMapping("/employee-reports")
	public String viewAppointments(Model model, HttpServletRequest request) {
		List<Employee> employeeList = employeeRepository.findAll()
			    .stream()
			    .filter(e -> e.getId() != 1L)
			    .collect(Collectors.toList());
		model.addAttribute("employeeList", employeeList);
		model.addAttribute("employeeStatusList", employeeStatusRepository.findAll());
		model.addAttribute("divisionList", divisionRepository.findAll());
		model.addAttribute("positionTitleList", positionTitleRepository.findAll());
		model.addAttribute("employee", new Employee());
		
		return "employee/reports/employee-list-reports";
		
	}
	
	@GetMapping("/separation-report")
	public String viewSeparationReport(Model model, HttpServletRequest request) {		
		return "employee/reports/separation-report";		
	}
	
	@GetMapping("/accession-report")
	public String viewAccessionReport(Model model, HttpServletRequest request) {		
		return "employee/reports/accession-report";		
	}
	
	//rai-report
	@GetMapping("/rai-report")
	public String viewRaiReport(Model model, HttpServletRequest request) {		
		return "employee/reports/rai-report";		
	}
	
	@GetMapping("/viewCs4Rpt")	
	public void viewCS4Reportv2(Model model, 
			@RequestParam String id,
            @RequestParam String signatoryName,
            @RequestParam String signatoryPosition, 
            @RequestParam String attestedBy,
            @RequestParam String attestedByPosition, 
            HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
		long employeeId = Long.parseLong(id);
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		
		ServiceRecord sr = serviceRecordRepository.findTopByEmployeeIdOrderByDateFromDesc(employeeId);
		
		List<Employee> dataList = new ArrayList<Employee>();		
		Employee dummyData = new Employee();
		dummyData.setFirstName("test");
		
		dataList.add(dummyData);
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		if(sr.getEntranceDate() != null) {
			// Get the month name
	        String monthName = sr.getEntranceDate().getMonth().toString();
	        
	        // Get the day of the month
	        int dayOfMonth = sr.getEntranceDate().getDayOfMonth();
	        
	        // Get the year
	        int year = sr.getEntranceDate().getYear();
	        
	        // Format the month name to title case (e.g., "JANUARY" to "January")
	        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1).toLowerCase();
	        String suffixDate = returnSuffixDate(dayOfMonth);
	        
	        map.put("date1", dayOfMonth + suffixDate);
			map.put("date2", monthName + " " + year);
		} else {
			map.put("date1", "");
			map.put("date2", "");
		}
		
		map.put("empName", employee.getFullName().toUpperCase());
		map.put("officeAssignment", getStringValue(sr.getOfficeAssignment()));
		
		String position = getStringValue(sr.getPositionTitle().getPositionTitleName()) + " " + getStringValue(sr.getPositionTitleNotes());
		
		map.put("position", position);		
		map.put("effectiveDate", formatDateWords(sr.getEntranceDate()));
		
		map.put("date3", formatDateWords(sr.getEntranceDate()));
		map.put("location", "Manila");
		map.put("signatory1", signatoryName);
		map.put("signatory2", attestedBy);
		map.put("position1", signatoryPosition);
		map.put("position2", attestedByPosition);
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
		
		response.setContentType("application/pdf");
		
		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/CSFORMNo4.jasper");
		
		
		if(reportStream == null){
			System.out.println("reportStream is NULL");
		}
		
		if(response.getOutputStream() == null){
			System.out.println("response.getOutputStream() is NULL");
		}
		
		JasperRunManager.runReportToPdfStream(reportStream,	response.getOutputStream(), map, beanColDataSource);
	}
	
	@GetMapping("/viewCsNo33")	
	public void viewCs33Report(Model model, 
			@RequestParam String id,
            @RequestParam String signatoryName,
            @RequestParam String signatoryPosition,			
			HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
		long employeeId = Long.parseLong(id);
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		
		ServiceRecord sr = serviceRecordRepository.findTopByEmployeeIdOrderByDateFromDesc(employeeId);
		
		List<Employee> dataList = new ArrayList<Employee>();		
		Employee dummyData = new Employee();
		dummyData.setFirstName("test");
		
		dataList.add(dummyData);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("empName", employee.getFullName().toUpperCase());
		map.put("employmentStatus", getStringValue(sr.getEmployeeStatus().getEmployeeStatusName()) + " " + getStringValue(sr.getEmploymentStatusNotes()));
		map.put("position", getStringValue(sr.getPositionTitle().getPositionTitleName()) + " " + getStringValue(sr.getPositionTitleNotes()));		
		map.put("office", getStringValue(sr.getOfficeAssignment()));
		double rateDbl = sr.getSalary();
		String rateInWords = convertAmountInWords(rateDbl);
		
		map.put("rate", rateInWords);
		map.put("appointmentStatus", getStringValue(sr.getStatusOfAppointment()));
		map.put("vice", getStringValue(sr.getVice()));
		map.put("viceStatus", getStringValue(sr.getStatusOfSepeparation()));
		map.put("plantillaNo", getStringValue(sr.getPlantillaNo()));
//		map.put("pageNo", sr.getPageNo() + "");
		map.put("pageNo", "");
		map.put("signatory", signatoryName);
		map.put("signatoryPosition", signatoryPosition);
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
		
		response.setContentType("application/pdf");
		
		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/CSFORMNo33-B.jasper");
		
		
		if(reportStream == null){
			System.out.println("reportStream is NULL");
		}
		
		if(response.getOutputStream() == null){
			System.out.println("response.getOutputStream() is NULL");
		}
		
		JasperRunManager.runReportToPdfStream(reportStream,	response.getOutputStream(), map, beanColDataSource);
	}
	
	@GetMapping("/viewEmploymentCertificate")
	public void viewEmploymentCertificate(Model model, 
			@RequestParam String id,
            @RequestParam String signatoryName,
            @RequestParam String signatoryPosition,		
			HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
		long employeeId = Long.parseLong(id);
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		
		ServiceRecord sr = serviceRecordRepository.findTopByEmployeeIdOrderByDateFromDesc(employeeId);
		
		List<Employee> dataList = new ArrayList<Employee>();		
		Employee dummyData = new Employee();
		dummyData.setFirstName("test");
		
		dataList.add(dummyData);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("empName", employee.getFullName().toUpperCase());
		map.put("empStatus", sr.getEmployeeStatus().getEmployeeStatusName());
		map.put("position", sr.getPositionTitle().getPositionTitleName());
		
		double rateDbl = sr.getSalary();
		String rateInWords = convertAmountInWords(rateDbl);
		
		map.put("rate", rateInWords);
		map.put("signatory", signatoryName);
		map.put("signatoryPosition", signatoryPosition);
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
		
		response.setContentType("application/pdf");
		
		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/CERTIFICATION.jasper");
		
		
		if(reportStream == null){
			System.out.println("reportStream is NULL");
		}
		
		if(response.getOutputStream() == null){
			System.out.println("response.getOutputStream() is NULL");
		}
		
		JasperRunManager.runReportToPdfStream(reportStream,	response.getOutputStream(), map, beanColDataSource);
	}
	
	@GetMapping("/viewEmployeeListReport")
	public void viewEmployeeListReport(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
	}
	
	@GetMapping("/viewServiceRecordRpt")
	public void viewServiceRecordReportRpt(Model model, @RequestParam String id,
            @RequestParam String notes,
            @RequestParam String printDate,
			HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
//		Optional<ServiceRecordReportRequest> optionalSrRequest = serviceRecordReportRequestRepository.findById(recordId);
//		ServiceRecordReportRequest srReportRequest = optionalSrRequest.orElseGet(() -> new ServiceRecordReportRequest());
		
		long employeeId = Long.parseLong(id);
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		
		List<ServiceRecord> list = serviceRecordRepository.findByEmployeeId(employeeId);
		
		
		List<ServiceRecordReportDto> reportList = new ArrayList<>(); 
		
		for(ServiceRecord sr : list) {
			reportList.add(convertToDto(sr));
		}
		
		Optional<ServiceRecordSignatory> srsOptional = serviceRecordSignatoryRepository.findAll().stream().findFirst();
		
		ServiceRecordSignatory srsObj = new ServiceRecordSignatory();
		if(srsOptional.isPresent()) {
			srsObj = srsOptional.get();		
		}
		
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Surname", employee.getLastName());
		map.put("Given_Name", employee.getFirstName());	
		map.put("Middle_Name", employee.getMiddleName());	
		map.put("BirthDate", formatDate(employee.getBirthdate()));	
		map.put("BirthPlace", employee.getBirthPlace());	
		map.put("Purpose", notes != null ? notes : "");	
		map.put("Officer", srsObj.getSignatory() != null ? srsObj.getSignatory() : "");	
		map.put("OfficerPosition", srsObj.getPosition() != null ? srsObj.getPosition() : "");	
		
//		map.put("Officer", "");	
//		map.put("OfficerPosition", "");	
		LocalDate date = LocalDate.parse(printDate);
		map.put("signDate", formatDate(date.plusDays(1)));	
		
//		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
//		
//		response.setContentType("application/pdf");
//		
//		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/ServiceRecord.jasper");
//		
//		
//		if(reportStream == null){
//			System.out.println("reportStream is NULL");
//		}
//		
//		if(response.getOutputStream() == null){
//			System.out.println("response.getOutputStream() is NULL");
//		}
//		
//		JasperRunManager.runReportToPdfStream(reportStream,	response.getOutputStream(), map, beanColDataSource);
		
		InputStream jrxmlStream = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream("jasper/reports/ServiceRecord.jrxml");

			if (jrxmlStream == null) {
				throw new RuntimeException("JRXML file not found: jasper/reports/ServiceRecord.jrxml");
			}

			JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
			JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportList);

			// Fill the report
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, dataSource);

			// Set response type and stream PDF output
			response.setContentType("application/pdf");
			try (ServletOutputStream outputStream = response.getOutputStream()) {
				JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
				outputStream.flush();
			}
		
	}
	
	@GetMapping("/viewServiceRecordReport/{employeeId}")
	public void viewServiceRecordReport(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, IOException {
		List<ServiceRecord> list = serviceRecordRepository.findByEmployeeId(employeeId);
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());

		List<ServiceRecordReportDto> reportList = new ArrayList<>();
		for (ServiceRecord sr : list) {
			reportList.add(convertToDto(sr));
		}
		
		Optional<ServiceRecordReportRequest> optionalServiceRecordRqst = serviceRecordReportRequestRepository.findFirstByEmployeeIdOrderByIdDesc(employeeId);
		
		Optional<ServiceRecordSignatory> srsOptional = serviceRecordSignatoryRepository.findAll().stream().findFirst();
		
		ServiceRecordSignatory srsObj = new ServiceRecordSignatory();
		if(srsOptional.isPresent()) {
			srsObj = srsOptional.get();		
		}
		
		ServiceRecordReportRequest serviceRecordRqstObj = new ServiceRecordReportRequest();
		if(optionalServiceRecordRqst.isPresent()) {
			serviceRecordRqstObj = optionalServiceRecordRqst.get();		
		}

		Map<String, Object> map = new HashMap<>();
		map.put("Surname", employee.getLastName());
		map.put("Given_Name", employee.getFirstName());	
		map.put("Middle_Name", employee.getMiddleName());	
		map.put("BirthDate", formatDate(employee.getBirthdate()));	
		map.put("BirthPlace", employee.getBirthPlace());	
		map.put("Purpose", serviceRecordRqstObj.getNotes());	
		map.put("Officer", srsObj.getSignatory());	
		map.put("OfficerPosition", srsObj.getPosition());

//		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
		map.put("signDate", serviceRecordRqstObj.getPrintDate().plusDays(1).format(formatter));

		// Load and compile the .jrxml at runtime
		InputStream jrxmlStream = Thread.currentThread()
			.getContextClassLoader()
			.getResourceAsStream("jasper/reports/ServiceRecord.jrxml");

		if (jrxmlStream == null) {
			throw new RuntimeException("JRXML file not found: jasper/reports/ServiceRecord.jrxml");
		}

		JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportList);

		// Fill the report
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, dataSource);

		// Set response type and stream PDF output
		response.setContentType("application/pdf");
		try (ServletOutputStream outputStream = response.getOutputStream()) {
			JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
			outputStream.flush();
		}
	}


	
	
//	@GetMapping("/viewServiceRecordReport/{employeeId}")
//	public void viewServiceRecordReport(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
//		List<ServiceRecord> list = serviceRecordRepository.findByEmployeeId(employeeId);
//		Optional<Employee> optional = employeeRepository.findById(employeeId);
//		Employee employee = optional.orElseGet(() -> new Employee());
//		
//		List<ServiceRecordReportDto> reportList = new ArrayList<>(); 
//		
//		for(ServiceRecord sr : list) {
//			reportList.add(convertToDto(sr));
//		}
//		
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("Surname", employee.getLastName());
//		map.put("Given_Name", employee.getFirstName());	
//		map.put("Middle_Name", employee.getMiddleName());	
//		map.put("BirthDate", formatDate(employee.getBirthdate()));	
//		map.put("BirthPlace", employee.getBirthPlace());	
//		map.put("Purpose", "1. Did not incur any leave of absence without pay. \r\n"
//				+ "2. This supersedes all previously submitted Service Records to GSIS.");	
//		map.put("Officer", "RIZALINO A. ABUSMAN");	
//		map.put("OfficerPosition", "Administrative Officer V");	
//		map.put("Officer", "RIZALINO A. ABUSMAN");
//		
//		LocalDate currentDate = LocalDate.now();
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
//		String formattedDate = currentDate.format(formatter);
//		
//		map.put("signDate", formattedDate);
//		
////		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
////		
////		response.setContentType("application/pdf");
////		
////		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/ServiceRecord.jasper");
////		
////		
////		if(reportStream == null){
////			System.out.println("reportStream is NULL");
////		}
////		
////		if(response.getOutputStream() == null){
////			System.out.println("response.getOutputStream() is NULL");
////		}
////		
////		JasperRunManager.runReportToPdfStream(reportStream,	response.getOutputStream(), map, beanColDataSource);
//		
//		
//		Map<String, Object> parameters = new HashMap<>();
//		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
//		InputStream reportStream = Thread.currentThread()
//		    .getContextClassLoader()
//		    .getResourceAsStream("jasper/reports/NewServiceRecord.jasper");
//
//		if (reportStream == null) {
//		    throw new RuntimeException("Report not found in classpath: jasper/reports/NewServiceRecord.jasper");
//		}
//
//		response.setContentType("application/pdf");
//		ServletOutputStream outputStream = response.getOutputStream();
//
//		JasperRunManager.runReportToPdfStream(reportStream, outputStream, parameters, beanColDataSource);
//		outputStream.flush();
//		outputStream.close();
//		
//	}
	
	
	
	@GetMapping("/viewCsNo4/{employeeId}")	
	public void viewCS4Report(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		
		ServiceRecord sr = serviceRecordRepository.findTopByEmployeeIdOrderByDateFromDesc(employeeId);
		
		List<Employee> dataList = new ArrayList<Employee>();		
		Employee dummyData = new Employee();
		dummyData.setFirstName("test");
		
		dataList.add(dummyData);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("empName", employee.getFullName().toUpperCase());
		map.put("officeAssignment", sr.getOfficeAssignment());
		map.put("position", sr.getPositionTitle().getPositionTitleName() + " " + sr.getPositionTitleNotes());		
		map.put("effectiveDate", formatDateWords(sr.getEntranceDate()));
		map.put("date1", "");
		map.put("date2", "");
		map.put("date3", "");
		map.put("location", "");
		map.put("signatory1", "");
		map.put("signatory2", "");
		map.put("position1", "");
		map.put("position2", "");
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
		
		response.setContentType("application/pdf");
		
		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/CSFORMNo4.jasper");
		
		
		if(reportStream == null){
			System.out.println("reportStream is NULL");
		}
		
		if(response.getOutputStream() == null){
			System.out.println("response.getOutputStream() is NULL");
		}
		
		JasperRunManager.runReportToPdfStream(reportStream,	response.getOutputStream(), map, beanColDataSource);
	}
	
	
	
	@GetMapping("/reports/employment-certificate/{employeeId}/{showMode}/{empHashCode}")
	public String viewEmployee(Model model, @PathVariable long employeeId, @PathVariable String showMode, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();

		if(optional.isPresent()) {		
			
			Employee employee = optional.orElseGet(() -> new Employee());
			
			model.addAttribute("employee", employee);
									
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}		
		
		return "reports/employment-certificate";
		
	}
	
	@GetMapping("/viewPds/{employeeId}")	
	public void viewPdsNew(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		
		List<FamilyBg> fbList = familyBgRepository.findByEmployeeId(employeeId);
		List<EducationalBackground> eduList = educationalBackgroundRepository.findByEmployeeId(employeeId);
		List<CivilServiceEligibility> csList = civilServiceEligibilityRepository.findByEmployeeId(employeeId);
		List<WorkExperience> workExList = workExperienceRepository.findByEmployeeIdOrderByDateFromDesc(employeeId);
		List<VoluntaryWork> voluntaryList = voluntaryWorkRepository.findByEmployeeId(employeeId);
		List<LearningAndDevelopment> learningList = learningAndDevelopmentRepository.findByEmployeeId(employeeId);
		List<OtherInfo> otherInfoList = otherInfoRepository.findByEmployeeId(employeeId);		
		List<OtherInfoQuestion> otherInfoQuestionList = otherInfoQuestionRepository.findByEmployeeId(employeeId);
		List<EmpReferences> refList = empReferencesRepository.findByEmployeeId(employeeId);
		List<GovermentIssuedId> govList = govermentIssuedIdRepository.findByEmployeeId(employeeId);
		
		OtherInfoQuestion otherObj = new OtherInfoQuestion();
		if(!otherInfoQuestionList.isEmpty()) {
			otherObj = otherInfoQuestionList.get(0);
		}
		
		List<ServiceRecordReportDto> reportList = new ArrayList<>(); 
		ServiceRecordReportDto x = new ServiceRecordReportDto();
		x.setStation("test");
		
		reportList.add(x);
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		
		response.setContentType("application/pdf");

		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS1.jasper");
		Map<String, Object> map = populateMapReport1(employee, fbList, eduList);
		
		InputStream reportStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS2.jasper");
		Map<String, Object> map2 = populateMapReport2(csList, workExList);
		
		InputStream reportStream3 = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS3.jasper");
		Map<String, Object> map3 = populateMapReport3(voluntaryList, learningList, otherInfoList);
		
		InputStream reportStream4 = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS4.jasper");
		Map<String, Object> map4 = populateMapReport4(otherObj, refList, govList);
		
		/////////
		
		JasperPrint jasperPrint1 = JasperFillManager.fillReport(reportStream, map, new JREmptyDataSource());
		JasperPrint jasperPrint2 = JasperFillManager.fillReport(reportStream2, map2, new JREmptyDataSource());
		JasperPrint jasperPrint3 = JasperFillManager.fillReport(reportStream3, map3, new JREmptyDataSource());
		JasperPrint jasperPrint4 = JasperFillManager.fillReport(reportStream4, map4, new JREmptyDataSource());
		
		System.out.println("Number of pages in report 1: " + jasperPrint1.getPages().size());
		System.out.println("Number of pages in report 2: " + jasperPrint2.getPages().size());
		System.out.println("Number of pages in report 3: " + jasperPrint3.getPages().size());
		System.out.println("Number of pages in report 4: " + jasperPrint4.getPages().size());

		
		List<JRPrintPage> pages2 = jasperPrint2.getPages();
        for (JRPrintPage page : pages2) {
            jasperPrint1.addPage(page);
        }
        
        List<JRPrintPage> pages3 = jasperPrint3.getPages();
        for (JRPrintPage page : pages3) {
            jasperPrint1.addPage(page);
        }
        
        List<JRPrintPage> pages4 = jasperPrint4.getPages();
        for (JRPrintPage page : pages4) {
            jasperPrint1.addPage(page);
        }

        JasperExportManager.exportReportToPdfStream(jasperPrint1, response.getOutputStream());

	}
	
	
	@GetMapping("/viewPdsxxxx/{employeeId}")	
	public void viewPds(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		
		List<FamilyBg> fbList = familyBgRepository.findByEmployeeId(employeeId);
		List<EducationalBackground> eduList = educationalBackgroundRepository.findByEmployeeId(employeeId);
		
		List<ServiceRecordReportDto> reportList = new ArrayList<>(); 
		ServiceRecordReportDto x = new ServiceRecordReportDto();
		x.setStation("test");
		
		reportList.add(x);
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		
		response.setContentType("application/pdf");

		InputStream pdsFullStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jasper/reports/PDSFULL.jasper");
		InputStream pds1Stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS1.jasper");
		InputStream pds2Stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS2.jasper");
		
		JasperReport pdsFullReport = (JasperReport) JRLoader.loadObject(pdsFullStream);
		JasperReport pds1Report = (JasperReport) JRLoader.loadObject(pds1Stream);
		JasperReport pds2Report = (JasperReport) JRLoader.loadObject(pds2Stream);
		
		
		
		if(pds1Stream == null){
			System.out.println("reportStream is NULL");
		}
		
		if(response.getOutputStream() == null){
			System.out.println("response.getOutputStream() is NULL");
		}
		
		if (pds1Stream == null || pds2Stream == null) {
            System.err.println("One or both reports could not be loaded.");
            return;
        }
		
		Map<String, Object> map = populateMapReport1(employee, fbList, eduList);
		
		File file2 = ResourceUtils.getFile("classpath:static/images/PDS2.png");
		String bgImg2 = file2.getAbsolutePath();
		map.put("V.CSE_Career_Service_RA_1080_1", "test");
		map.put("FormBg2", bgImg2);
		
		map.put("PDS1", pds1Report);
		map.put("PDS2", pds2Report);
		
		SequenceInputStream mergedStream = new SequenceInputStream(pds1Stream, pds2Stream);
		
		JasperPrint pdsFullPrint = JasperFillManager.fillReport(pdsFullReport, map, new JREmptyDataSource());
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(pdsFullPrint, outputStream);
        
        // Write the PDF stream to output or do whatever you need with it
        response.getOutputStream().write(outputStream.toByteArray());
        
//		JasperPrint pds1Print = JasperFillManager.fillReport(pds1Report, map, beanColDataSource);
//		JasperPrint pds2Print = JasperFillManager.fillReport(pds2Report, map, beanColDataSource);
//		
//		JasperPrint mergedPrint = new JasperPrint();
//        mergedPrint.addPage(pds1Print.getPages().get(0));
//        mergedPrint.addPage(pds2Print.getPages().get(0));
//        
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        JasperExportManager.exportReportToPdfStream(mergedPrint, outputStream);
//        
//        response.getOutputStream().write(outputStream.toByteArray());
		
//		JasperRunManager.runReportToPdfStream(pds2Stream,	response.getOutputStream(), map, beanColDataSource);
//		JasperRunManager.runReportToPdfStream(pds2Stream,	response.getOutputStream(), map, beanColDataSource);
	}
	
	private Map<String, Object> populateMapReport1(Employee emp, List<FamilyBg> fbList, List<EducationalBackground> eduList) throws FileNotFoundException {
		File file = ResourceUtils.getFile("classpath:static/images/PDS1.png");
		String bgImg = file.getAbsolutePath();
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("FormBg", bgImg);
		
		map.put("CS_ID_no.", "");
		map.put("I.PI_Surname", "  " + emp.getLastName());
		map.put("I.PI_Firstname", "  " + emp.getFirstName());
		map.put("I.PI_NameExtension", "  " + emp.getSuffix());
		map.put("I.PI_Middlename", "  " + emp.getMiddleName());
		map.put("I.PI_Date_Of_Birth", "  " + formatDate(emp.getBirthdate()));
		map.put("I.PI_Place_Of_Birth", "  " + emp.getBirthPlace());
		
		if("M".equalsIgnoreCase(emp.getGender())) {
			map.put("I.PI_Sex_M", " X");
			map.put("I.PI_Sex_F", "");
		} else {
			map.put("I.PI_Sex_M", "");
			map.put("I.PI_Sex_F", " X");
		}
		
		if("SINGLE".equalsIgnoreCase(emp.getCivilStatus())) {
			map.put("I.PI_Civil_Status_Single", " X");
			map.put("I.PI_Civil_Status_Widowed", "");
			map.put("I.PI_Civil_Status_Others", "");
			map.put("I.PI_Civil_Status_Others_Text", "");
			map.put("I.PI_Civil_Status_Married", "");
			map.put("I.PI_Civil_Status_Separated", "");
		} else if("MARRIED".equalsIgnoreCase(emp.getCivilStatus())) {
			map.put("I.PI_Civil_Status_Single", "");
			map.put("I.PI_Civil_Status_Widowed", "");
			map.put("I.PI_Civil_Status_Others", "");
			map.put("I.PI_Civil_Status_Others_Text", "");
			map.put("I.PI_Civil_Status_Married", " X");
			map.put("I.PI_Civil_Status_Separated", "");
		} else if("WIDOWED".equalsIgnoreCase(emp.getCivilStatus())) {
			map.put("I.PI_Civil_Status_Single", "");
			map.put("I.PI_Civil_Status_Widowed", " X");
			map.put("I.PI_Civil_Status_Others", "");
			map.put("I.PI_Civil_Status_Others_Text", "");
			map.put("I.PI_Civil_Status_Married", "");
			map.put("I.PI_Civil_Status_Separated", "");			
		} else if("SEPARATED".equalsIgnoreCase(emp.getCivilStatus())) {
			map.put("I.PI_Civil_Status_Single", "");
			map.put("I.PI_Civil_Status_Widowed", "");
			map.put("I.PI_Civil_Status_Others", "");
			map.put("I.PI_Civil_Status_Others_Text", "");
			map.put("I.PI_Civil_Status_Married", "");
			map.put("I.PI_Civil_Status_Separated", " X");
		} else {
			map.put("I.PI_Civil_Status_Single", "");
			map.put("I.PI_Civil_Status_Widowed", "");
			map.put("I.PI_Civil_Status_Others", " X");
			map.put("I.PI_Civil_Status_Others_Text", ""); //TODO fix this
			map.put("I.PI_Civil_Status_Married", "");
			map.put("I.PI_Civil_Status_Separated", "");
		}
		
		
		map.put("I.PI_Height", "  " + getStringValue(emp.getHeight()));
		map.put("I.PI_Weight", "  " + getStringValue(emp.getWeight()));
		map.put("I.PI_Bloodtype", "  " + getStringValue(emp.getBloodType()));
		map.put("I.PI_GSIS_ID_NO.", "  " + getStringValue(emp.getGsisIdNo()));
		map.put("I.PI_Pagibig_ID_NO.", "  " + getStringValue(emp.getPagibigNo()));
		map.put("I.PI_PhilHealth_NO.", "  " + getStringValue(emp.getPhilhealthNo()));
		map.put("I.PI_SSS_NO.", "  " + getStringValue(emp.getSssNo()));
		map.put("I.PI_TIN_NO.", "  " + getStringValue(emp.getTin()));
		map.put("I.PI_Agency_Employee_NO.", "  " + getStringValue(emp.getEmpNo()));
		
		//Residential Address
		//Get Province Map
		
		
		map.put("PI_Residential_House_No", "  " + getStringValue(emp.getHouseno1()));
		map.put("PI_Residential_Street", "  " + getStringValue(emp.getStreet1()));
		map.put("PI_Residential_Subdivision", "  " + getStringValue(emp.getSubdivision1()));
		map.put("PI_Residential_Barangay", "  " + getStringValue(emp.getBrgy1()));
		map.put("PI_Residential_City", "  " + getStringValue(emp.getCity1()));
		map.put("PI_Residential_Province", "  " + getStringValueProvince(emp.getProvince1()));
		map.put("PI_Residential_Zipcode", "  " + getStringValue(emp.getZipcode1()));
		
		map.put("PI_Permanent_House_No", "  " + getStringValue(emp.getHouseno2()));
		map.put("PI_Permanent_Street", "  " + getStringValue(emp.getStreet2()));
		map.put("PI_Permanent_Subdivision", "  " + getStringValue(emp.getSubdivision2()));
		map.put("PI_Permanent_Barangay", "  " + getStringValue(emp.getBrgy2()));
		map.put("PI_Permanent_City", "  " + getStringValue(emp.getCity2()));
		map.put("PI_Permanent_Province", "  " + getStringValueProvince(emp.getProvince2()));
		map.put("PI_Permanent_Zipcode", "  " + getStringValue(emp.getZipcode2()));
		
		//TODO Fix this
		if("FILIPINO BY BIRTH".equalsIgnoreCase(emp.getCitizenship())) {
			map.put("I.PI_Citizenship_Filipino", " X");
			map.put("I.PI_Citizenship_Dual", "");
			map.put("I.PI_Citizenship_By_birth", "");
			map.put("I.PI_Citizenship_By_naturalization", "");
			map.put("I.PI_Citizenship_Indicate_Country", "");
		} else if("FILIPINO BY NATURALIZATION".equalsIgnoreCase(emp.getCitizenship())) {
			map.put("I.PI_Citizenship_Filipino", "");
			map.put("I.PI_Citizenship_Dual", " X");
			map.put("I.PI_Citizenship_By_birth", "");
			map.put("I.PI_Citizenship_By_naturalization", " X");
			map.put("I.PI_Citizenship_Indicate_Country", "");
		} else if("DUAL CITIZENSHIP BY BIRTH".equalsIgnoreCase(emp.getCitizenship())) {
			map.put("I.PI_Citizenship_Filipino", "");
			map.put("I.PI_Citizenship_Dual", " X");
			map.put("I.PI_Citizenship_By_birth", " X");
			map.put("I.PI_Citizenship_By_naturalization", "");
			map.put("I.PI_Citizenship_Indicate_Country", "");
		} else {
			map.put("I.PI_Citizenship_Filipino", "");
			map.put("I.PI_Citizenship_Dual", "");
			map.put("I.PI_Citizenship_By_birth", "");
			map.put("I.PI_Citizenship_By_naturalization", "");
			map.put("I.PI_Citizenship_Indicate_Country", "");
		}
		
		
		map.put("I.PI_Telephone_NO", "  " + emp.getTelNo() != null ? emp.getTelNo() : "");
		map.put("I.PI_Mobile_NO", "  " + emp.getMobileNo1() != null ? emp.getMobileNo1() : "");
		map.put("I.PI_EmailAdd", "  " + emp.getEmail1() != null ? emp.getEmail1() : "");
		
		int ctrForChild = 1;
		boolean spousePopulated = false;
		boolean fatherPopulated = false;
		boolean motherPopulated = false;
		
		//Family
		for(FamilyBg fb : fbList) {
			if("SPOUSE".equalsIgnoreCase(fb.getRelationship())) {
				map.put("II.FBG_Spouse_Surname", "  " + fb.getLastName());
				map.put("II.FBG_Spouse_Firstname", "  " + fb.getFirstName());
				map.put("II.FBG_Spouse_Name_Extension", "  " + fb.getSuffix());
				map.put("II.FBG_Spouse_Middlename", "  " + fb.getMiddleName());
				map.put("II.FBG_Spouse_Occupation", "  " + fb.getOccupation());
				map.put("II.FBG_Spouse_Employer", "  " + fb.getEmployer());
				map.put("II.FBG_Spouse_Business_Address", "  " + fb.getBusinessAdd());
				map.put("II.FBG_Spouse_TelephoneNO", "  " + fb.getTelNo());
				spousePopulated = true;
			} else if("FATHER".equalsIgnoreCase(fb.getRelationship())) {
				fatherPopulated = true;
				map.put("II.FBG_Father_Surname", "  " + fb.getLastName());
				map.put("II.FBG_Father_Firstname", "  " + fb.getFirstName());
				map.put("II.FBG_Father_Name_Extension", "  " + fb.getSuffix());
				map.put("II.FBG_Father_Middlename", "  " + fb.getMiddleName());
			} else if("MOTHER".equalsIgnoreCase(fb.getRelationship())) {
				map.put("II.FBG_Mother_Maidenname", "");
				map.put("II.FBG_Mother_Surname", "  " + fb.getLastName());
				map.put("II.FBG_Mother_Firstname", "  " + fb.getFirstName());
				map.put("II.FBG_Mother_Middlename", "  " + fb.getMiddleName());
				motherPopulated = true;
			} else {
				map.put("II.FBG_Child_name"+ctrForChild, "  " + fb.getFirstName() + " " + fb.getMiddleName() + " " + fb.getLastName());				
				map.put("II.FBG_Child_Birthday"+ctrForChild, "  " + formatDate(fb.getBirthdate()) );
				ctrForChild++;
			}			
		}
		
		if(motherPopulated) {
			
		} else {
			map.put("II.FBG_Mother_Maidenname", "");
			map.put("II.FBG_Mother_Surname", "");
			map.put("II.FBG_Mother_Firstname", "");
			map.put("II.FBG_Mother_Middlename", "");
		}
		
		if(fatherPopulated) {
			
		} else {
			map.put("II.FBG_Father_Surname", "");
			map.put("II.FBG_Father_Firstname", "");
			map.put("II.FBG_Father_Name_Extension", "");
			map.put("II.FBG_Father_Middlename", "");
		}

		if(spousePopulated) {
			
		} else {
			map.put("II.FBG_Spouse_Surname", "");
			map.put("II.FBG_Spouse_Firstname", "");
			map.put("II.FBG_Spouse_Name_Extension", "");
			map.put("II.FBG_Spouse_Middlename", "");
			map.put("II.FBG_Spouse_Occupation", "");
			map.put("II.FBG_Spouse_Employer", "");
			map.put("II.FBG_Spouse_Business_Address", "");
			map.put("II.FBG_Spouse_TelephoneNO", "");
		}
		
		if(ctrForChild < 12) {
			for(int x = ctrForChild; x <= 12; x++) {
				map.put("II.FBG_Child_name"+x, "");				
				map.put("II.FBG_Child_Birthday"+x, "");
			}
		}		
		
		//Education BG
		boolean elemPopulated = false;
		boolean secPopulated = false;
		boolean vocPopulated = false;
		boolean collegePopulated = false;
		boolean gradopulated = false;
		
		for(EducationalBackground eb : eduList) {
			String unitsEarned = "";
			
			if(eb.getUnitsEarned() != null && eb.getUnitsEarned().length() > 0) {
				if(!eb.getUnitsEarned().equalsIgnoreCase("GRADUATE")) {
					unitsEarned = eb.getUnitsEarned();
				}
			}
			
			if("ELEMENTARY".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				elemPopulated = true;
				map.put("III.EB_Elementary_School", " " + eb.getSchool().getSchoolName());
				
				if(eb.getDegreeCourse() != null) {
					if(eb.getDegreeCourse().getDegreeCourseName() != null) {
						map.put("III.EB_Elementary_BasicEducation_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
					} else {
						map.put("III.EB_Elementary_BasicEducation_Degree_Course", "");
					}
				} else {
					map.put("III.EB_Elementary_BasicEducation_Degree_Course", "");
				}
				
				map.put("III.EB_Elementary_Period_Of_Attendance_From", "    " + formatDateYearOnly(eb.getStartDate()));
				map.put("III.EB_Elementary_Period_Of_Attendance_To", "    " + formatDateYearOnly(eb.getEndDate()));
				map.put("III.EB_Elementary_HighestLvl_UnitsEarned", "    " + unitsEarned);
				map.put("III.EB_Elementary_Year_Graduated", " " + "    " + formatDateYearOnly(eb.getEndDate()));
				
				if(eb.getScholarship() != null) {
					if(eb.getScholarship().getScholarshipName() != null) {
						map.put("III.EB_Elementary_Scholarship_Acad_Honors_Recieved", " " + eb.getScholarship().getScholarshipName());
					} else {
						map.put("III.EB_Elementary_Scholarship_Acad_Honors_Recieved", "");
					}
				} else {
					map.put("III.EB_Elementary_Scholarship_Acad_Honors_Recieved", "");
				}
				
				
			} else if("SECONDARY".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName()) || "HIGH SCHOOL".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				secPopulated = true;
				map.put("III.EB_Secondary_School", " " + eb.getSchool().getSchoolName());
				
				if(eb.getDegreeCourse() != null) {
					if(eb.getDegreeCourse().getDegreeCourseName() != null) {
						map.put("III.EB_Secondary_BasicEducation_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
					} else {
						map.put("III.EB_Secondary_BasicEducation_Degree_Course", "");
					}
				} else {
					map.put("III.EB_Secondary_BasicEducation_Degree_Course", "");
				}
				
				map.put("III.EB_Secondary_Period_Of_Attendance_From",  "    " + formatDateYearOnly(eb.getStartDate()));
				map.put("III.EB_Secondary_Period_Of_Attendance_To", "    " + formatDateYearOnly(eb.getEndDate()));
				map.put("III.EB_Secondary_HighestLvl_UnitsEarned", "    " + unitsEarned);
				map.put("III.EB_Secondary_Year_Graduated", " " + "    " + formatDateYearOnly(eb.getEndDate()));
				
				if(eb.getScholarship() != null) {
					if(eb.getScholarship().getScholarshipName() != null) {
						map.put("III.EB_Secondary_Scholarship_Academic_Honors_Recieved", " " + eb.getScholarship().getScholarshipName());
					} else {
						map.put("III.EB_Secondary_Scholarship_Academic_Honors_Recieved", "");
					}
				} else {
					map.put("III.EB_Secondary_Scholarship_Academic_Honors_Recieved", "");
				}
			} else if("VOCATIONAL".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				vocPopulated = true;
				map.put("III.EB_Vocational_TradeCourse_School", " " + eb.getSchool().getSchoolName());
				
				if(eb.getDegreeCourse() != null) {
					if(eb.getDegreeCourse().getDegreeCourseName() != null) {
						map.put("III.EB_Vocational_TradeCourse_Basic_Education_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
					} else {
						map.put("III.EB_Vocational_TradeCourse_Basic_Education_Degree_Course", "");
					}
				} else {
					map.put("III.EB_Vocational_TradeCourse_Basic_Education_Degree_Course", "");
				}
				
				map.put("III.EB_Vocational_TradeCourse_Period_Of_Attendance_From",  "    " + formatDateYearOnly(eb.getStartDate()));
				map.put("III.EB_Vocational_TradeCourse_Period_Of_Attendance_To", "    " + formatDateYearOnly(eb.getEndDate()));
				map.put("III.EB_Vocational_TradeCourse_HighestLvl_UnitsEarned", "    " + unitsEarned);
				map.put("III.EB_Vocational_TradeCourse_Year_Graduated", "    " + formatDateYearOnly(eb.getEndDate()));

				if(eb.getScholarship() != null) {
					if(eb.getScholarship().getScholarshipName() != null) {
						map.put("III.EB_Vocational_TradeCourse_Scholarship_Academic_Honors_Received", " " + eb.getScholarship().getScholarshipName());
					} else {
						map.put("III.EB_Vocational_TradeCourse_Scholarship_Academic_Honors_Received", "");
					}
				} else {
					map.put("III.EB_Vocational_TradeCourse_Scholarship_Academic_Honors_Received", "");
				}
			} else if("COLLEGE".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				collegePopulated = true;
				map.put("III.EB_College_School", " " + eb.getSchool().getSchoolName());
				
				if(eb.getDegreeCourse() != null) {
					if(eb.getDegreeCourse().getDegreeCourseName() != null) {
						map.put("III.EB_College_BasicEducation_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
					} else {
						map.put("III.EB_College_BasicEducation_Degree_Course", "");
					}
				} else {
					map.put("III.EB_College_BasicEducation_Degree_Course", "");
				}
				
				map.put("III.EB_College_Period_Of_Attendance_From",  "    " + formatDateYearOnly(eb.getStartDate()));
				map.put("III.EB_College_Period_Of_Attendance_To", "    " + formatDateYearOnly(eb.getEndDate()));
				map.put("III.EB_College_HighestLvl_UnitsEarned", "    " + unitsEarned);
				map.put("III.EB_College_Year_Graduated", "    " + formatDateYearOnly(eb.getEndDate()));
				
				if(eb.getScholarship() != null) {
					if(eb.getScholarship().getScholarshipName() != null) {
						map.put("III.EB_College_Scholarship_Academic_Honors_Received", " " + eb.getScholarship().getScholarshipName());
					} else {
						map.put("III.EB_College_Scholarship_Academic_Honors_Received", "");
					}
				} else {
					map.put("III.EB_College_Scholarship_Academic_Honors_Received", "");
				}
								
			} else if("GRADUATE STUDIES".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				gradopulated = true;
				map.put("III.EB_GraduateStudies_School", " " + eb.getSchool().getSchoolName());
				
				if(eb.getDegreeCourse() != null) {
					if(eb.getDegreeCourse().getDegreeCourseName() != null) {
						map.put("III.EB_GraduateStudies_BasicEducation_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
					} else {
						map.put("III.EB_GraduateStudies_BasicEducation_Degree_Course", "");
					}
				} else {
					map.put("III.EB_GraduateStudies_BasicEducation_Degree_Course", "");
				}
				
				map.put("III.EB_GraduateStudies_Period_Of_Attendance_From",  "    " + formatDateYearOnly(eb.getStartDate()));
				map.put("III.EB_GraduateStudies_Period_Of_Attendance_To ",  "    " + formatDateYearOnly(eb.getEndDate()));
				map.put("III.EB_GraduateStudies_HighestLvl_UnitsEarned", "    " + unitsEarned);
				map.put("III.EB_GraduateStudies_Year_Graduated", "    " + formatDateYearOnly(eb.getEndDate()));
				
				if(eb.getScholarship() != null) {
					if(eb.getScholarship().getScholarshipName() != null) {
						map.put("III.EB_GraduateStudies_Scholarship_Academic_Honors_Received", " " + eb.getScholarship().getScholarshipName());
					} else {
						map.put("III.EB_GraduateStudies_Scholarship_Academic_Honors_Received", "");
					}
				} else {
					map.put("III.EB_GraduateStudies_Scholarship_Academic_Honors_Received", "");
				}
			}
		}
		
		if(elemPopulated) {
			
		} else {
			map.put("III.EB_Elementary_School", "");
			map.put("III.EB_Elementary_BasicEducation_Degree_Course", "");
			map.put("III.EB_Elementary_Period_Of_Attendance_From", "");
			map.put("III.EB_Elementary_Period_Of_Attendance_To", "");
			map.put("III.EB_Elementary_HighestLvl_UnitsEarned", "");
			map.put("III.EB_Elementary_Year_Graduated", "");
			map.put("III.EB_Elementary_Scholarship_Acad_Honors_Recieved", "");
		}
		
		if(secPopulated) {
			
		} else {
			map.put("III.EB_Secondary_School", "");
			map.put("III.EB_Secondary_BasicEducation_Degree_Course", "");
			map.put("III.EB_Secondary_Period_Of_Attendance_From",  "");
			map.put("III.EB_Secondary_Period_Of_Attendance_To", "");
			map.put("III.EB_Secondary_HighestLvl_UnitsEarned", "");
			map.put("III.EB_Secondary_Year_Graduated", "");
			map.put("III.EB_Secondary_Scholarship_Academic_Honors_Recieved", "");
		}
		
		if(vocPopulated) {
			
		} else {
			map.put("III.EB_Vocational_TradeCourse_School", "");
			map.put("III.EB_Vocational_TradeCourse_Basic_Education_Degree_Course",  "");
			map.put("III.EB_Vocational_TradeCourse_Period_Of_Attendance_From",  "");
			map.put("III.EB_Vocational_TradeCourse_Period_Of_Attendance_To", "");
			map.put("III.EB_Vocational_TradeCourse_HighestLvl_UnitsEarned", "");
			map.put("III.EB_Vocational_TradeCourse_Year_Graduated", "");
			map.put("III.EB_Vocational_TradeCourse_Scholarship_Academic_Honors_Received", "");
		}
		
		if(collegePopulated) {
			
		} else {
			map.put("III.EB_College_School", "");
			map.put("III.EB_College_BasicEducation_Degree_Course",  "");
			map.put("III.EB_College_Period_Of_Attendance_From",  "");
			map.put("III.EB_College_Period_Of_Attendance_To", "");
			map.put("III.EB_College_HighestLvl_UnitsEarned", "");
			map.put("III.EB_College_Year_Graduated", "");
			map.put("III.EB_College_Scholarship_Academic_Honors_Received", "");
		}
		
		if(gradopulated) {
			
		} else {
			map.put("III.EB_GraduateStudies_School", "");
			map.put("III.EB_GraduateStudies_BasicEducation_Degree_Course",  "");
			map.put("III.EB_GraduateStudies_Period_Of_Attendance_From",  "");
			map.put("III.EB_GraduateStudies_Period_Of_Attendance_To ",  "");
			map.put("III.EB_GraduateStudies_HighestLvl_UnitsEarned", "");
			map.put("III.EB_GraduateStudies_Year_Graduated", "");
			map.put("III.EB_GraduateStudies_Scholarship_Academic_Honors_Received", "");
		}
		
		
		
		map.put("III.EB_Signature", "");
		map.put("III.EB_Date", "");
		map.put("CopyOFParameter_30", "");
		map.put("CopyOFParameter_31", "");
		map.put("CopyOFParameter_32", "");
		map.put("CopyOFParameter_33", "");
		map.put("CopyOFParameter_34", "");
		map.put("CopyOFParameter_35", "");
		map.put("CopyOFParameter_36", "");
		map.put("CopyOFParameter_37", "");	
		
		
		
		return map;
	}
	
	private Map<String, Object> populateMapReport2(List<CivilServiceEligibility> csList, List<WorkExperience> workExList) throws FileNotFoundException {
		Map<String, Object> map = new HashMap<String, Object>();
		File file = ResourceUtils.getFile("classpath:static/images/PDS2.png");
		String bgImg = file.getAbsolutePath();
		
		map.put("FormBg2", bgImg);
		
		int ctrForCS = 1;
		for(CivilServiceEligibility cs : csList) {
			map.put("IV.CSE_Career_Service_RA_1080_"+ctrForCS, " " + getStringValue(cs.getEligibility()) );
			map.put("IV.CSE_Rating"+ctrForCS, " " + getStringValue(cs.getRating()) );
			map.put("IV.CSE_Date_Of_Examination"+ctrForCS, " " + getStringValue(cs.getExamDate()) );
			map.put("IV.CSE_Place_Of_Examination"+ctrForCS, " " + getStringValue(cs.getPlaceOfExam()) );
			map.put("IV.CSE_License_Number_"+ctrForCS, " " + getStringValue(cs.getLicenseNo()) );
			map.put("IV.CSE_License_Date_Of_Validity"+ctrForCS, " " + formatDateMonthYearOnly(cs.getLicenseValidityDate()) );
			ctrForCS++;
		}
		
		if(ctrForCS < 7) {
			for(int x = ctrForCS; x <= 7; x++) {				
				map.put("IV.CSE_Career_Service_RA_1080_"+x, "");
				map.put("IV.CSE_Rating"+x, "");
				map.put("IV.CSE_Date_Of_Examination"+x, "");
				map.put("IV.CSE_Place_Of_Examination"+x, "");
				map.put("IV.CSE_License_Number_"+x, "");
				map.put("IV.CSE_License_Date_Of_Validity"+x, "");
			}
		}
		
		int ctrForWorkEx = 1;
		for(WorkExperience we : workExList) {
			map.put("V.WE_Inclusive_Dates_From"+ctrForWorkEx, " " + formatDateMonthYearOnly(we.getDateFrom()) );
			map.put("V.WE_Inclusive_Dates_To"+ctrForWorkEx, " " + formatDateMonthYearOnly(we.getDateTo()) );
			map.put("V.WE_Position_Title"+ctrForWorkEx, "     " + getStringValue(we.getPositionTitle()) );
			map.put("V.WE_Department_Agency_Office_Company"+ctrForWorkEx, "     " + getStringValue(we.getDepartment()) );
			map.put("V.WE_Montly_Salary"+ctrForWorkEx, "        " + getStringValueFromBigDecimal(we.getSalary()) );
//			map.put("V.WE_Salary_Job_PayGrade"+ctrForWorkEx, "        " + getStringValue(we.getSalaryGrade() + "") );
			
			String salaryGradeStr = we.getSalaryGrade() == 0 ? "N/A" : String.valueOf(we.getSalaryGrade());
			map.put("V.WE_Salary_Job_PayGrade" + ctrForWorkEx, "        " + getStringValue(salaryGradeStr));

			
			map.put("V.WE_Status_Of_Appointment"+ctrForWorkEx, "   " + getStringValue(we.getAppointmentStatus()) );
			map.put("V.WE_Gov_Service"+ctrForWorkEx, "        " + getStringValue(we.getGovtOffice()) );
			ctrForWorkEx++;
		}
		
		if(ctrForWorkEx < 28) {
			for(int x = ctrForWorkEx; x <= 28; x++) {				
				map.put("V.WE_Inclusive_Dates_From"+x, "");
				map.put("V.WE_Inclusive_Dates_To"+x, "");
				map.put("V.WE_Position_Title"+x, "");
				map.put("V.WE_Department_Agency_Office_Company"+x, "");
				map.put("V.WE_Montly_Salary"+x, "");
				map.put("V.WE_Salary_Job_PayGrade"+x, "");
				map.put("V.WE_Status_Of_Appointment"+x, "");
				map.put("V.WE_Gov_Service"+x, "");
			}
		}
		
		map.put("V.WE_SIGNATURE", "");
		map.put("V.WE_DATE", "");
		
		
		return map;
	}
	
	private Map<String, Object> populateMapReport3(List<VoluntaryWork> voluntaryList, List<LearningAndDevelopment> learningList, List<OtherInfo> otherList) throws FileNotFoundException {
		Map<String, Object> map = new HashMap<String, Object>();
		File file = ResourceUtils.getFile("classpath:static/images/PDS3.png");
		String bgImg = file.getAbsolutePath();
		
		map.put("FormBg3", bgImg);
		
		int ctrForVw = 1;
		for(VoluntaryWork vw : voluntaryList) {
			map.put("VI.VW_Name_Address_Of_Org"+ctrForVw, " " + getStringValue(vw.getOrgName()) );
			map.put("VI.VW_Inclusive_Dates_From"+ctrForVw, " " + formatDateMonthYearOnly(vw.getDateFrom()) );
			map.put("VI.VW_Inclusive_Dates_To"+ctrForVw, " " + formatDateMonthYearOnly(vw.getDateTo()) );
			map.put("VI.VW_Number_Of_Hours"+ctrForVw, " " + getStringValue(vw.getNoHours() + "") );
			map.put("VI.VW_Position_Nature_Of_Work"+ctrForVw, " " + getStringValue(vw.getNatureOfWork()) );
			ctrForVw++;
		}
		
		if(ctrForVw < 7) {
			for(int x = ctrForVw; x <= 7; x++) {				
				map.put("VI.VW_Name_Address_Of_Org"+x, "");
				map.put("VI.VW_Inclusive_Dates_From"+x, "");
				map.put("VI.VW_Inclusive_Dates_To"+x, "");
				map.put("VI.VW_Number_Of_Hours"+x, "");
				map.put("VI.VW_Position_Nature_Of_Work"+x, "");
			}
		}
		
		int ctrForLd = 1;
		for(LearningAndDevelopment ld : learningList) {
			map.put("VII.LAD_Training_Programs"+ctrForLd, " " + getStringValue(ld.getTitleOfSeminar()) );
			map.put("VII.LAD_Inclusive_Dates_Of_Attendance_From"+ctrForLd, " " + formatDateMonthYearOnly(ld.getDateFrom()) );
			map.put("VII.LAD_Inclusive_Dates_Of_Attendance_To"+ctrForLd, " " + formatDateMonthYearOnly(ld.getDateTo()) );			
			map.put("VII.LAD_Number_Of_Hours"+ctrForLd,  " " + getStringValue(ld.getNoHours() + "") );
			map.put("VII.LAD_Type_Of_LD"+ctrForLd, " " + getStringValue(ld.getLearningType()) );
			map.put("VII.Conducted_Sponsored_By"+ctrForLd, " " + getStringValue(ld.getProviders()) );
			ctrForLd++;
		}
		
		if(ctrForLd < 21) {
			for(int x = ctrForLd; x <= 21; x++) {				
				map.put("VII.LAD_Training_Programs"+x, "");
				map.put("VII.LAD_Inclusive_Dates_Of_Attendance_From"+x, "");
				map.put("VII.LAD_Inclusive_Dates_Of_Attendance_To"+x, "");	
				map.put("VII.LAD_Number_Of_Hours"+x, "");
				map.put("VII.LAD_Type_Of_LD"+x, "");
				map.put("VII.Conducted_Sponsored_By"+x, "");
			}
		}
		
		int ctrForOtherInfo = 1;
		for(OtherInfo oi : otherList) {
			map.put("VIII.OI_Special_Skills_Hobbies"+ctrForOtherInfo, " " + getStringValue(oi.getSpecialSkill()) );
			map.put("VIII.OI_NonAcademic_Distinctions_Recognitions"+ctrForOtherInfo, " " + getStringValue(oi.getNonAcademic()) );
			map.put("VIII.OI_Membership_In_Association"+ctrForOtherInfo, " " + getStringValue(oi.getMembershipInAssociation()) );
			ctrForOtherInfo++;
		}
		
		if(ctrForOtherInfo < 7) {
			for(int x = ctrForOtherInfo; x <= 7; x++) {				
				map.put("VIII.OI_Special_Skills_Hobbies"+x, "");
				map.put("VIII.OI_NonAcademic_Distinctions_Recognitions"+x, "");
				map.put("VIII.OI_Membership_In_Association"+x, "");
			}
		}
		
		map.put("VIII.OI_Signature", "");
		map.put("VIII.OI_Date", "");
		
		
		return map;
	}
	
	private Map<String, Object> populateMapReport4(OtherInfoQuestion otherInfoQuestion, List<EmpReferences> referencesList, List<GovermentIssuedId> govIdList) throws FileNotFoundException {
		Map<String, Object> map = new HashMap<String, Object>();
		File file = ResourceUtils.getFile("classpath:static/images/PDS4.png");
		String bgImg = file.getAbsolutePath();
		
		map.put("FormBg4", bgImg);
		
		//34 A
		if(otherInfoQuestion.getQuestionOneThird() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionOneThird())) {
				map.put("VIII.OI_34_A_Yes", "X");
				map.put("VIII.OI_34_A_No", "");
			} else {
				map.put("VIII.OI_34_A_Yes", "");
				map.put("VIII.OI_34_A_No", "X");
			}
		} else {
			map.put("VIII.OI_34_A_Yes", "");
			map.put("VIII.OI_34_A_No", "");
		}
		
		//34 B
		if(otherInfoQuestion.getQuestionOneFourth() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionOneFourth())) {
				map.put("VIII.OI_34_B_Yes", "X");
				map.put("VIII.OI_34_B_No", "");
				if(otherInfoQuestion.getQuestionOneFourthIfYes() != null 
						&& otherInfoQuestion.getQuestionOneFourthIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionOneFourthIfYes())) {
					map.put("VIII.OI_34_If_Yes", otherInfoQuestion.getQuestionOneFourthIfYes());
				} else {
					map.put("VIII.OI_34_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_34_B_Yes", "");
				map.put("VIII.OI_34_B_No", "X");
				map.put("VIII.OI_34_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_34_B_Yes", "");
			map.put("VIII.OI_34_B_No", "");
			map.put("VIII.OI_34_If_Yes", "");
		}
		
		//35 A
		if(otherInfoQuestion.getQuestionTwoA() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionTwoA())) {
				map.put("VIII.OI_35_A_Yes", "X");
				map.put("VIII.OI_35_A_No", "");
				if(otherInfoQuestion.getQuestionTwoAIfYes() != null 
						&& otherInfoQuestion.getQuestionTwoAIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionTwoAIfYes())) {
					map.put("VIII.OI_35_A_If_Yes", otherInfoQuestion.getQuestionTwoAIfYes());
				} else {
					map.put("VIII.OI_35_A_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_35_A_Yes", "");
				map.put("VIII.OI_35_A_No", "X");
				map.put("VIII.OI_35_A_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_35_A_Yes", "");
			map.put("VIII.OI_35_A_No", "");
			map.put("VIII.OI_35_A_If_Yes", "");
		}
		
		//35 B
		if(otherInfoQuestion.getQuestionTwoB() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionTwoB())) {
				map.put("VIII.OI_35_B_Yes", "X");
				map.put("VIII.OI_35_B_No", "");
				if(otherInfoQuestion.getQuestionTwoBIfYes() != null 
						&& otherInfoQuestion.getQuestionTwoBIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionTwoBIfYes())) {
					map.put("VIII.OI_35_Date_Filed", otherInfoQuestion.getQuestionTwoBMonth() + " " + otherInfoQuestion.getQuestionTwoBDay() + " " + otherInfoQuestion.getQuestionTwoBYear());
					map.put("VIII.OI_35_Status_Of_Cases", otherInfoQuestion.getQuestionTwoBStatusCase());
				} else {
					map.put("VIII.OI_35_Date_Filed", "");
					map.put("VIII.OI_35_Status_Of_Cases", "");
				}
			} else {
				map.put("VIII.OI_35_B_Yes", "");
				map.put("VIII.OI_35_B_No", "X");
				map.put("VIII.OI_35_Date_Filed", "");
				map.put("VIII.OI_35_Status_Of_Cases", "");
			}
		} else {
			map.put("VIII.OI_35_B_Yes", "");
			map.put("VIII.OI_35_B_No", "");
			map.put("VIII.OI_35_Date_Filed", "");
			map.put("VIII.OI_35_Status_Of_Cases", "");			
		}
		
		//36	
		if(otherInfoQuestion.getQuestionThree() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionThree())) {
				map.put("VIII.OI_36_A_Yes", "X");
				map.put("VIII.OI_36_A_No", "");
				if(otherInfoQuestion.getQuestionThreeIfYes() != null 
						&& otherInfoQuestion.getQuestionThreeIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionThreeIfYes())) {
					map.put("VIII.OI_36_If_Yes", otherInfoQuestion.getQuestionThreeIfYes());
				} else {
					map.put("VIII.OI_36_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_36_A_Yes", "");
				map.put("VIII.OI_36_A_No", "X");
				map.put("VIII.OI_36_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_36_A_Yes", "");
			map.put("VIII.OI_36_A_No", "");
			map.put("VIII.OI_36_If_Yes", "");
		}
		
		//37
		if(otherInfoQuestion.getQuestionFour() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionFour())) {
				map.put("VIII.OI_37_A_Yes", "X");
				map.put("VIII.OI_37_A_No", "");
				if(otherInfoQuestion.getQuestionFourIfYes() != null 
						&& otherInfoQuestion.getQuestionFourIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionFourIfYes())) {
					map.put("VIII.OI_37_A_If_Yes", otherInfoQuestion.getQuestionFourIfYes());
				} else {
					map.put("VIII.OI_37_A_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_37_A_Yes", "");
				map.put("VIII.OI_37_A_No", "X");
				map.put("VIII.OI_37_A_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_37_A_Yes", "");
			map.put("VIII.OI_37_A_No", "");
			map.put("VIII.OI_37_A_If_Yes", "");
		}
		
		//38 A
		if(otherInfoQuestion.getQuestionFive() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionFive())) {
				map.put("VIII.OI_38_A_Yes", "X");
				map.put("VIII.OI_38_A_No", "");
				if(otherInfoQuestion.getQuestionFiveIfYes() != null 
						&& otherInfoQuestion.getQuestionFiveIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionFiveIfYes())) {
					map.put("VIII.OI_38_A_If_Yes", otherInfoQuestion.getQuestionFiveIfYes());
				} else {
					map.put("VIII.OI_38_A_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_38_A_Yes", "");
				map.put("VIII.OI_38_A_No", "X");
				map.put("VIII.OI_38_A_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_38_A_Yes", "");
			map.put("VIII.OI_38_A_No", "");
			map.put("VIII.OI_38_A_If_Yes", "");
		}
		
		//38 B
		if(otherInfoQuestion.getQuestionSix() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionSix())) {
				map.put("VIII.OI_38_B_Yes", "X");
				map.put("VIII.OI_38_B_No", "");
				if(otherInfoQuestion.getQuestionSixIfYes() != null 
						&& otherInfoQuestion.getQuestionSixIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionSixIfYes())) {
					map.put("VIII.OI_38_B_If_Yes", otherInfoQuestion.getQuestionSixIfYes());
				} else {
					map.put("VIII.OI_38_B_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_38_B_Yes", "");
				map.put("VIII.OI_38_B_No", "X");
				map.put("VIII.OI_38_B_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_38_B_Yes", "");
			map.put("VIII.OI_38_B_No", "");
			map.put("VIII.OI_38_B_If_Yes", "");
		}
		
		//39
		if(otherInfoQuestion.getQuestionSevenA() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionSevenA())) {
				map.put("VIII.OI_39_A_Yes", "X");
				map.put("VIII.OI_39_A_No", "");
				if(otherInfoQuestion.getQuestionSevenAIfYes() != null 
						&& otherInfoQuestion.getQuestionSevenAIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionSevenAIfYes())) {
					map.put("VIII.OI_39_A_If_Yes", otherInfoQuestion.getQuestionSevenAIfYes());
				} else {
					map.put("VIII.OI_39_A_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_39_A_Yes", "");
				map.put("VIII.OI_39_A_No", "X");
				map.put("VIII.OI_39_A_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_39_A_Yes", "");
			map.put("VIII.OI_39_A_No", "");
			map.put("VIII.OI_39_A_If_Yes", "");
		}
		
		//40 C
		if(otherInfoQuestion.getQuestionNine() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionNine())) {
				map.put("VIII.OI_40_C_Yes", "X");
				map.put("VIII.OI_40_C_No", "");
				if(otherInfoQuestion.getQuestionNineIfYes() != null 
						&& otherInfoQuestion.getQuestionNineIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionNineIfYes())) {
					map.put("VIII.OI_40_C_If_Yes", otherInfoQuestion.getQuestionNineIfYes());
				} else {
					map.put("VIII.OI_40_C_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_40_C_Yes", "");
				map.put("VIII.OI_40_C_No", "X");
				map.put("VIII.OI_40_C_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_40_C_Yes", "");
			map.put("VIII.OI_40_C_No", "");
			map.put("VIII.OI_40_C_If_Yes", "");
		}
		
		//40 A
		if(otherInfoQuestion.getQuestionTen() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionTen())) {
				map.put("VIII.OI_40_A_Yes", "X");
				map.put("VIII.OI_40_A_No", "");
				if(otherInfoQuestion.getQuestionTenIfYes() != null 
						&& otherInfoQuestion.getQuestionTenIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionTenIfYes())) {
					map.put("VIII.OI_40_A_If_Yes", otherInfoQuestion.getQuestionTenIfYes());
				} else {
					map.put("VIII.OI_40_A_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_40_A_Yes", "");
				map.put("VIII.OI_40_A_No", "X");
				map.put("VIII.OI_40_A_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_40_A_Yes", "");
			map.put("VIII.OI_40_A_No", "");
			map.put("VIII.OI_40_A_If_Yes", "");
		}
		
		//40 B
		if(otherInfoQuestion.getQuestionEight() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionEight())) {
				map.put("VIII.OI_40_B_Yes", "X");
				map.put("VIII.OI_40_B_No", "");
				if(otherInfoQuestion.getQuestionEightId() != null 
						&& otherInfoQuestion.getQuestionEightId().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionEightId())) {
					map.put("VIII.OI_40_B_If_Yes", otherInfoQuestion.getQuestionEightId());
				} else {
					map.put("VIII.OI_40_B_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_40_B_Yes", "");
				map.put("VIII.OI_40_B_No", "X");
				map.put("VIII.OI_40_B_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_40_B_Yes", "");
			map.put("VIII.OI_40_B_No", "");
			map.put("VIII.OI_40_B_If_Yes", "");
		}
		
		
		
		//References
		int ctrForRef = 1;
		for(EmpReferences ref : referencesList) {
			map.put("VIII.OI_41_References_Name"+ctrForRef, " " + getStringValue(ref.getReferenceName()) );
			map.put("VIII.OI_41_References_Address"+ctrForRef, " " + getStringValue(ref.getCompanyAddress()) );
			map.put("VIII.OI_41_References_Tel_No"+ctrForRef, " " + getStringValue(ref.getCompanyContactNo()) );
			ctrForRef++;
		}
		
		if(ctrForRef < 3) {
			for(int x = ctrForRef; x <= 7; x++) {				
				map.put("VIII.OI_41_References_Name"+x, "");
				map.put("VIII.OI_41_References_Address"+x, "");
				map.put("VIII.OI_41_References_Tel_No"+x, "");
			}
		}
		
		if (!govIdList.isEmpty()) {
            // Return the first item in the list
			GovermentIssuedId obj =  govIdList.get(0);
			map.put("VIII.OI_42_Gov_ID", obj.getGovermentIssuedName());
			map.put("VIII.OI_42_ID_License_Passport_No", obj.getIdNo());
			map.put("VIII.OI_42_Date_Place_Of_Issurance", obj.getPlaceOfIssuance());
			
		} else {
			map.put("VIII.OI_42_Gov_ID", "");
			map.put("VIII.OI_42_ID_License_Passport_No", "");
			map.put("VIII.OI_42_Date_Place_Of_Issurance", "");
		}
		
		map.put("VIII.OI_42_Signature", "");
		map.put("VIII.OI_42_Date_Accomplished", "");
		map.put("VIII.OI_42_Thumbmark", "");
		map.put("Subscribed_Sworn", "");
		map.put("Person_Administering Oath", "");
		
		//map.put("", "");
		
		return map;
	}
	
	@GetMapping("/viewClearanceForm/{id}")
	public void viewClearanceReport(Model model, @PathVariable long id, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
		Optional<ClearanceApprovers> optional = clearanceApproversRepository.findAll().stream().findFirst();
		
		ClearanceApprovers clearanceApprovers = new ClearanceApprovers();
		if(optional.isPresent()) {
			clearanceApprovers = optional.get();		
		}
		
		Optional<Clearance> oClearance = clearanceRepository.findById(id);
		Clearance clearance = oClearance.orElseGet(() -> new Clearance());
				
		Map<String, Object> map = new HashMap<String, Object>();
		
		
		String empName = clearance.getEmployee().getFirstName() + " " +  clearance.getEmployee().getLastName();
		
				
		map.put("City_Gov", HEADER_REPORT_NAME);
		map.put("To_City_Gov", HEADER_REPORT_NAME);
		map.put("Date1", formatDate(clearance.getTransDate()));		
		map.put("Date_Effect", formatDate(clearance.getEffectiveDate()));		
		
		map.put("Office_Of_Assignment", clearance.getEmployee().getDivision().getDivisionName());
		map.put("Position_SG_Step", clearance.getEmployee().getPositionTitle().getPositionTitleName());
		map.put("Name_Signature_Employee", empName.toUpperCase());
						
		String immediateSupervisor = clearanceApprovers.getImmediateSupervisor();
				
		map.put("Immediate_Supervisor", immediateSupervisor);
				
		String headOfOffice = clearanceApprovers.getHeadOfOffice();
				
		map.put("Head_Of_Office", headOfOffice);
		
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
		
		String clearingOfficer1A = clearanceApprovers.getAdminPersonA();		
		String clearingOfficer1B = clearanceApprovers.getAdminPersonB();		
		String clearingOfficer1C = clearanceApprovers.getAdminPersonC();		
		
		String clearingOfficer2A = clearanceApprovers.getLibraryPersonA();		
		String clearingOfficer2B = clearanceApprovers.getLibraryPersonB();
		
		String clearingOfficer3A = clearanceApprovers.getFinancePersonA();		
		String clearingOfficer3B = clearanceApprovers.getFinancePersonB();				
		String clearingOfficer3C = clearanceApprovers.getFinancePersonC();		
		
		String clearingOfficer4A = clearanceApprovers.getProfessionalPersonA();	
		
		String clearingOfficer5A = clearanceApprovers.getSection4Person();
		
		//1
		if(clearingOfficer1A != null && clearingOfficer1A.length() > 0) {
			map.put("1A_Name_Clearing_Officer", clearingOfficer1A + "\n" + clearanceApprovers.getAdminPositionA());
//			map.put("1A_Name_Clearing_Officer", "RIZALINO A. ABUSMAN\nAdministrative Officer V");
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
			map.put("1B_Name_Clearing_Officer", clearingOfficer1B + "\n" + clearanceApprovers.getAdminPositionB());
//			map.put("1B_Name_Clearing_Officer", "ROSALINDA C. MANOJO\nAdministrative Officer V");
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
			map.put("1C_Name_Clearing_Officer", clearingOfficer1C + "\n" + clearanceApprovers.getAdminPositionC());
//			map.put("1C_Name_Clearing_Officer", "");
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
			map.put("2A_Name_Clearing_Officer", clearingOfficer2A + "\n" + clearanceApprovers.getLibraryPositionA());
//			map.put("2A_Name_Clearing_Officer", "HECTOR R. PASCUAL\nAdministrative Officer V");
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
			map.put("2B_Name_Clearing_Officer", clearingOfficer2B + "\n" + clearanceApprovers.getLibraryPositionB());
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
			map.put("3A_Name_Clearing_Officer", clearingOfficer3A + "\n" + clearanceApprovers.getFinancePositionA());
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
			map.put("3B_Name_Clearing_Officer", clearingOfficer3B + "\n" + clearanceApprovers.getFinancePositionB());
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
			map.put("3C_Name_Clearing_Officer", clearingOfficer3C + "\n" + clearanceApprovers.getFinancePositionC());
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
			map.put("4A_Name_Clearing_Officer", clearingOfficer4A + "\n" + clearanceApprovers.getProfessionalPositionA());
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
			map.put("IV_CONPAC_A_Name_Clearing_Officer", clearingOfficer4A + "\n" + clearanceApprovers.getSection4Position());
//			map.put("IV_CONPAC_A_Name_Clearing_Officer", "CHARITO A. RUMBO\nChief Administrative Officer");
			map.put("IV_CONPAC_A_Cleared", "");
			map.put("IV_CONPAC_A_Not_Cleared", "");
			map.put("IV_CONPAC_A_Signature", "");
		} else {
			map.put("IV_CONPAC_A_Name_Clearing_Officer", "N/A");
			map.put("IV_CONPAC_A_Cleared", "N/A");
			map.put("IV_CONPAC_A_Not_Cleared", "N/A");
			map.put("IV_CONPAC_A_Signature", "N/A");
		}
		
		String footerSignatory2 = clearanceApprovers.getFooterPerson1();		
		
		map.put("V_CERTIFICATION_NAME2", footerSignatory2);
//		map.put("V_CERTIFICATION_NAME2", "LUCH R. GEMPIS JR.");
		map.put("V_CERTIFICATION_POSITION2", clearanceApprovers.getFooterPosition2());
		map.put("V_CERTIFICATION_OFFICE2", "Secretary to the City Council");
		
		String footerSignatory1 = clearanceApprovers.getFooterPerson1();
		
		map.put("V_CERTIFICATION_NAME1", footerSignatory1);
//		map.put("V_CERTIFICATION_NAME1", "ROMEO N. FRANCIA");
		map.put("V_CERTIFICATION_POSITION1", clearanceApprovers.getFooterPosition1());
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
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy");
		
		if(localDate != null) {			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			return localDate.format(formatter);
		}
		
		return "";
	}
	
	private static String formatDateMonthYearOnly(LocalDate localDate) {
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy");		
		
		if(localDate != null) {			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
			return localDate.format(formatter);
		}
		
		return "";
	}
	
	private static String formatDateYearOnly(LocalDate localDate) {
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy");		
		
		if(localDate != null) {			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
			return localDate.format(formatter);
		}
		
		return "";
	}
	
	private static String formatDateWords(LocalDate localDate) {		
		if(localDate != null) {			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
			return localDate.format(formatter);
		}
		
		return "";
	}
	
	private static ServiceRecordReportDto convertToDto(ServiceRecord serviceRecord) {
        ServiceRecordReportDto dto = new ServiceRecordReportDto();
        dto.setDateFrom(formatDate(serviceRecord.getDateFrom()));
        dto.setDateTo(formatDate(serviceRecord.getDateTo()));
        dto.setDesignation(serviceRecord.getDesignation() != null ? serviceRecord.getDesignation() : "");
        dto.setEmployeeStatus(serviceRecord.getEmployeeStatus().getEmployeeStatusName());
        dto.setSalary(getFormattedAmount(serviceRecord.getSalary())); // Format salary to two decimal places
        dto.setStation(serviceRecord.getStation());
        dto.setBranch(serviceRecord.getBranch() != null ? serviceRecord.getBranch() : "");
        dto.setLvAbs(serviceRecord.getLvAbs() != null ? serviceRecord.getLvAbs() : "");
        dto.setSeparationCause(serviceRecord.getSeparationCause() != null ? serviceRecord.getSeparationCause() : "");
        dto.setSeparationDate(formatDate(serviceRecord.getSeparationDate()));
        return dto;
    }
	
	private static String getFormattedAmount(Object obj){
		if(obj != null) {			
			DecimalFormat decimalFormat = new DecimalFormat("#,###,###.00");
			return decimalFormat.format(obj);
		}
		
		return "";		
	}
	
	private static String getFormattedAmountWithoutDecimal(Object obj){
		if(obj != null) {			
			DecimalFormat decimalFormat = new DecimalFormat("#,###,###");
			return decimalFormat.format(obj);
		}
		
		return "";		
	}
	
	public static Map<String, String> generateProvinceMap() {
        Map<String, String> provinceMap = new HashMap<>();

        // Add province data to the map
        provinceMap.put("MM", "Metro Manila");
        provinceMap.put("ABR", "Abra");
        provinceMap.put("APA", "Apayao");
        provinceMap.put("BEN", "Benguet");
        provinceMap.put("IFU", "Ifugao");
        provinceMap.put("KAL", "Kalinga");
        provinceMap.put("MOU", "Mountain Province");
        provinceMap.put("ILN", "Ilocos Norte");
        provinceMap.put("ILS", "Ilocos Sur");
        provinceMap.put("LUN", "La Union");
        provinceMap.put("PAN", "Pangasinan");
        provinceMap.put("BTN", "Batanes");
        provinceMap.put("CAG", "Cagayan");
        provinceMap.put("ISA", "Isabela");
        provinceMap.put("NUV", "Nueva Vizcaya");
        provinceMap.put("QUI", "Quirino");
        provinceMap.put("AUR", "Aurora");
        provinceMap.put("BAN", "Bataan");
        provinceMap.put("BUL", "Bulacan");
        provinceMap.put("NUE", "Nueva Ecija");
        provinceMap.put("PAM", "Pampanga");
        provinceMap.put("TAR", "Tarlac");
        provinceMap.put("ZMB", "Zambales");
        provinceMap.put("BTG", "Batangas");
        provinceMap.put("CAV", "Cavite");
        provinceMap.put("LAG", "Laguna");
        provinceMap.put("QUE", "Quezon");
        provinceMap.put("RIZ", "Rizal");
        provinceMap.put("MAD", "Marinduque");
        provinceMap.put("MDC", "Occidental Mindoro");
        provinceMap.put("MDR", "Oriental Mindoro");
        provinceMap.put("PLW", "Palawan");
        provinceMap.put("ROM", "Romblon");
        provinceMap.put("ALB", "Albay");
        provinceMap.put("CAN", "Camarines Norte");
        provinceMap.put("CAS", "Camarines Sur");
        provinceMap.put("CAT", "Catanduanes");
        provinceMap.put("MAS", "Masbate");
        provinceMap.put("SOR", "Sorsogon");
        provinceMap.put("AKL", "Aklan");
        provinceMap.put("ANT", "Antique");
        provinceMap.put("CAP", "Capiz");
        provinceMap.put("GUI", "Guimaras");
        provinceMap.put("ILI", "Iloilo");
        provinceMap.put("NEC", "Negros Occidental");
        provinceMap.put("BOH", "Bohol");
        provinceMap.put("CEB", "Cebu");
        provinceMap.put("NER", "Negros Oriental");
        provinceMap.put("SIG", "Siquijor");
        provinceMap.put("BIL", "Biliran");
        provinceMap.put("EAS", "Eastern Samar");
        provinceMap.put("LEY", "Leyte");
        provinceMap.put("NSA", "Northern Samar");
        provinceMap.put("WSA", "Samar");
        provinceMap.put("SLE", "Southern Leyte");
        provinceMap.put("ZAN", "Zamboanga del Norte");
        provinceMap.put("ZAS", "Zamboanga del Sur");
        provinceMap.put("ZSI", "Zamboanga Sibugay");
        provinceMap.put("BUK", "Bukidnon");
        provinceMap.put("CAM", "Camiguin");
        provinceMap.put("LAN", "Lanao del Norte");
        provinceMap.put("MSC", "Misamis Occidental");
        provinceMap.put("MSR", "Misamis Oriental");
        provinceMap.put("COM", "Compostela Valley");
        provinceMap.put("DAV", "Davao del Norte");
        provinceMap.put("DAS", "Davao del Sur");
        provinceMap.put("DAC", "Davao Occidental");
        provinceMap.put("DAO", "Davao Oriental");
        provinceMap.put("NCO", "Cotabato");
        provinceMap.put("SAR", "Sarangani");
        provinceMap.put("SCO", "South Cotabato");
        provinceMap.put("SUK", "Sultan Kudarat");
        provinceMap.put("AGN", "Agusan del Norte");
        provinceMap.put("AGS", "Agusan del Sur");
        provinceMap.put("DIN", "Dinagat Islands");
        provinceMap.put("SUN", "Surigao del Norte");
        provinceMap.put("SUR", "Surigao del Sur");
        provinceMap.put("BAS", "Basilan");
        provinceMap.put("LAS", "Lanao del Sur");
        provinceMap.put("MAG", "Maguindanao");
        provinceMap.put("SLU", "Sulu");
        provinceMap.put("TAW", "Tawi-tawi");

        return provinceMap;
    }
	
	private static String getStringValueFromBigDecimal(BigDecimal val) {
		if(val != null) {
			DecimalFormat decimalFormat = new DecimalFormat("#,###,###.##");
			return decimalFormat.format(val);
		} else {
			return "";
		}		
	}
	
	private static String getStringValueFromInt(Integer val) {
		if(val != null) {
			return val.toString();
		} else {
			return "";
		}		
	}
	
	private static String getStringValue(String val) {
		if(val != null) {
			if("null".equalsIgnoreCase(val.trim())) {
				return "";
			} else {
				return val;
			}
		} else {
			return "";
		}		
	}
	
	private static String getStringValueProvince(String val) {
		Map<String, String> provinceMap = generateProvinceMap();
		
		if(val != null) {
			if("null".equalsIgnoreCase(val.trim())) {
				return "";
			} else {
				return provinceMap.get(val);
			}
		} else {
			return "";
		}		
	}
	
	//Convert Amount to Words
	// Array for numbers less than 20
    private static final String[] belowTwenty = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
    };

    // Array for tens
    private static final String[] tens = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    // Array for big numbers
    private static final String[] bigNumbers = {
            "", "Thousand", "Million", "Billion"
    };

    // Convert a number less than 1000 to words
    private static String convertLessThanThousand(int num) {
        String current;

        if (num % 100 < 20) {
            current = belowTwenty[num % 100];
            num /= 100;
        } else {
            current = belowTwenty[num % 10];
            num /= 10;

            current = tens[num % 10] + (current.isEmpty() ? "" : " " + current);
            num /= 10;
        }

        if (num == 0) return current;
        return belowTwenty[num] + " Hundred" + (current.isEmpty() ? "" : " and " + current);
    }

    // Convert the integer part to words
    private static String convertIntegerPart(int num) {
        if (num == 0) return "Zero";

        String prefix = "";
        if (num < 0) {
            num = -num;
            prefix = "Negative ";
        }

        String current = "";
        int place = 0;

        do {
            int n = num % 1000;
            if (n != 0) {
                String s = convertLessThanThousand(n);
                current = s + (bigNumbers[place].isEmpty() ? "" : " " + bigNumbers[place]) + (current.isEmpty() ? "" : " " + current);
            }
            place++;
            num /= 1000;
        } while (num > 0);

        return prefix + current.trim();
    }

    // Convert the fractional part to words
    private static String convertFractionalPart(int num) {
        return convertLessThanThousand(num);
    }

    // Convert the entire amount to words
    public static String convertAmountInWords(double amount) {
        int integerPart = (int) amount;
        int fractionalPart = (int) Math.round((amount - integerPart) * 100);

        String integerPartInWords = convertIntegerPart(integerPart);
        String formattedAmount = new DecimalFormat("#,###,###.00").format(amount);

        if (fractionalPart == 0) {
            return String.format("%s (%s)", integerPartInWords, formattedAmount);
        } else {
            String fractionalPartInWords = convertFractionalPart(fractionalPart);
            return String.format("%s and %s/100 (%s)", integerPartInWords, fractionalPartInWords, formattedAmount);
        }
    }

    public static void main(String[] args) {
//        double amount1 = 12345.67;
//        double amount2 = 12345.00;
//        System.out.println("Amount in words: " + convertAmountInWords(amount1)); // Should include fractional part and formatted amount
//        System.out.println("Amount in words: " + convertAmountInWords(amount2)); // Should exclude fractional part but include formatted amount
    	
    	LocalDate localDate = LocalDate.now(); // Get today's date
        
        // Get the month name
        String monthName = localDate.getMonth().toString();
        
        // Get the day of the month
        int dayOfMonth = localDate.getDayOfMonth();
        
        // Get the year
        int year = localDate.getYear();
        
        // Format the month name to title case (e.g., "JANUARY" to "January")
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1).toLowerCase();
        
        // Print the results
        System.out.println("Month name: " + monthName);
        System.out.println("Day of the month: " + dayOfMonth);
        System.out.println("Year: " + year);
    }
    
    private static String returnSuffixDate(int dayOfMonth) {
    	String daySuffix;
        switch (dayOfMonth % 10) {
            case 1:
                daySuffix = "st";
                break;
            case 2:
                daySuffix = "nd";
                break;
            case 3:
                daySuffix = "rd";
                break;
            default:
                daySuffix = "th";
                break;
        }
        
        return daySuffix;
    }

}
