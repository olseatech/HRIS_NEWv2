package com.ian.web.reports;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.SequenceInputStream;
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
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.clearance.Clearance;
import com.ian.web.employee.clearance.ClearanceApprovers;
import com.ian.web.employee.clearance.ClearanceApproversRepository;
import com.ian.web.employee.clearance.ClearanceRepository;
import com.ian.web.employee.educationalbg.EducationalBackground;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.eligibility.CivilServiceEligibility;
import com.ian.web.employee.eligibility.CivilServiceEligibilityRepository;
import com.ian.web.employee.familybg.FamilyBg;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.govermentid.GovermentIssuedId;
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfo;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestion;
import com.ian.web.employee.references.EmpReferences;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.servicerecord.ServiceRecord;
import com.ian.web.employee.servicerecord.ServiceRecordReportDto;
import com.ian.web.employee.servicerecord.ServiceRecordRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperience;
import com.ian.web.employee.workexperience.WorkExperienceRepository;
import com.ian.web.systemsettings.division.DivisionRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.position_title.PositionTitleRepository;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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
	private final ClearanceApproversRepository clearanceApproversRepository;
	
	private final ResourceLoader resourceLoader;
	
	
	@GetMapping("/viewPds/{employeeId}")	
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
		map.put("I.PI_Surname", "  " + emp.getLastName().toUpperCase());
		map.put("I.PI_Firstname", "  " + emp.getFirstName().toUpperCase());
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
		
		
		map.put("I.PI_Height", "  " + emp.getHeight());
		map.put("I.PI_Weight", "  " + emp.getWeight());
		map.put("I.PI_Bloodtype", "  " + emp.getBloodType());
		map.put("I.PI_GSIS_ID_NO.", "  " + emp.getGsisIdNo());
		map.put("I.PI_Pagibig_ID_NO.", "  " + emp.getPagibigNo());
		map.put("I.PI_PhilHealth_NO.", "  " + emp.getPhilhealthNo());
		map.put("I.PI_SSS_NO.", "  " + emp.getSssNo());
		map.put("I.PI_TIN_NO.", "  " + emp.getTin());
		map.put("I.PI_Agency_Employee_NO.", "  " + emp.getEmpNo());
		
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
		
		map.put("I.PI_Residential_House_Block_Lot_NO", "");
		map.put("I.PI_Residential_Street", "");
		map.put("I.PI_Residential_Subdivision_Village", "");
		map.put("I.PI_Residential_Barangay", "");
		map.put("I.PI_Residential_City", "");
		map.put("I.PI_Residential_Province", "");
		map.put("I.PI_Residential_ZIP_CODE", "");
		map.put("I.PI_Permanent_House_Block_Lot_NO", "");
		map.put("I.PI_Permanent_Street", "");
		map.put("I.PI_Permanent_Subdivision_Village", "");
		map.put("I.PI_Permanent_Barangay", "");
		map.put("I.PI_Permanent_City", "");
		map.put("I.PI_Permanent_Province", "");
		map.put("I.PI_Permanent_ZIP_CODE", "");
		map.put("I.PI_Telephone_NO", "  " + emp.getTelNo());
		map.put("I.PI_Mobile_NO", "  " + emp.getMobileNo1());
		map.put("I.PI_EmailAdd", "  " + emp.getEmail1());
		
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
			if("ELEMENTARY".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				elemPopulated = true;
				map.put("III.EB_Elementary_School", " " + eb.getSchool().getSchoolName());
				map.put("III.EB_Elementary_BasicEducation_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
				map.put("III.EB_Elementary_Period_Of_Attendance_From", " " + eb.getStartDate());
				map.put("III.EB_Elementary_Period_Of_Attendance_To", " " + eb.getEndDate());
				map.put("III.EB_Elementary_HighestLvl_UnitsEarned", " " + eb.getUnitsEarned());
				map.put("III.EB_Elementary_Year_Graduated", " " + eb.getYearGraduated());
				map.put("III.EB_Elementary_Scholarship_Acad_Honors_Recieved", " " + eb.getScholarship().getScholarshipName());
			} else if("SECONDARY".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				secPopulated = true;
				map.put("III.EB_Secondary_School", " " + eb.getSchool().getSchoolName());
				map.put("III.EB_Secondary_BasicEducation_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
				map.put("III.EB_Secondary_Period_Of_Attendance_From",  " " + eb.getStartDate());
				map.put("III.EB_Secondary_Period_Of_Attendance_To", " " + eb.getEndDate());
				map.put("III.EB_Secondary_HighestLvl_UnitsEarned", " " + eb.getUnitsEarned());
				map.put("III.EB_Secondary_Year_Graduated", " " + eb.getYearGraduated());
				map.put("III.EB_Secondary_Scholarship_Academic_Honors_Recieved", " " + eb.getScholarship().getScholarshipName());
			} else if("VOCATIONAL".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				vocPopulated = true;
				map.put("III.EB_Vocational_TradeCourse_School", " " + eb.getSchool().getSchoolName());
				map.put("III.EB_Vocational_TradeCourse_Basic_Education_Degree_Course",  " " + eb.getDegreeCourse().getDegreeCourseName());
				map.put("III.EB_Vocational_TradeCourse_Period_Of_Attendance_From",  " " + eb.getStartDate());
				map.put("III.EB_Vocational_TradeCourse_Period_Of_Attendance_To", " " + eb.getEndDate());
				map.put("III.EB_Vocational_TradeCourse_HighestLvl_UnitsEarned", " " + eb.getUnitsEarned());
				map.put("III.EB_Vocational_TradeCourse_Year_Graduated", " " + eb.getYearGraduated());
				map.put("III.EB_Vocational_TradeCourse_Scholarship_Academic_Honors_Received", " " + eb.getScholarship().getScholarshipName());
			} else if("COLLEGE".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				collegePopulated = true;
				map.put("III.EB_College_School", " " + eb.getSchool().getSchoolName());
				map.put("III.EB_College_BasicEducation_Degree_Course",  " " + eb.getDegreeCourse().getDegreeCourseName());
				map.put("III.EB_College_Period_Of_Attendance_From",  " " + eb.getStartDate());
				map.put("III.EB_College_Period_Of_Attendance_To", " " + eb.getEndDate());
				map.put("III.EB_College_HighestLvl_UnitsEarned", " " + eb.getUnitsEarned());
				map.put("III.EB_College_Year_Graduated", " " + eb.getYearGraduated());
				map.put("III.EB_College_Scholarship_Academic_Honors_Received", " " + eb.getScholarship().getScholarshipName());
			} else {
				gradopulated = true;
				map.put("III.EB_GraduateStudies_School", " " + eb.getSchool().getSchoolName());
				map.put("III.EB_GraduateStudies_BasicEducation_Degree_Course",  " " + eb.getDegreeCourse().getDegreeCourseName());
				map.put("III.EB_GraduateStudies_Period_Of_Attendance_From",  " " + eb.getStartDate());
				map.put("III.EB_GraduateStudies_Period_Of_Attendance_To ",  " " + eb.getEndDate());
				map.put("III.EB_GraduateStudies_HighestLvl_UnitsEarned", " " + eb.getUnitsEarned());
				map.put("III.EB_GraduateStudies_Year_Graduated", " " + eb.getYearGraduated());
				map.put("III.EB_GraduateStudies_Scholarship_Academic_Honors_Received", " " + eb.getScholarship().getScholarshipName());
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
	
	private Map<String, Object> populateMapReport2(List<CivilServiceEligibility> csList, List<WorkExperience> workExList, List<OtherInfo> otherInfoList) throws FileNotFoundException {
		return null;
	}
	
	private Map<String, Object> populateMapReport3(OtherInfoQuestion otherInfoQuestion, List<EmpReferences> referencesList, List<GovermentIssuedId> govIdList) throws FileNotFoundException {
		return null;
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
		
		Optional<ClearanceApprovers> optional = clearanceApproversRepository.findAll().stream().findFirst();
		
		ClearanceApprovers clearanceApprovers = new ClearanceApprovers();
		if(optional.isPresent()) {
			clearanceApprovers = optional.get();		
		}
		
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
		map.put("Name_Signature_Employee", empName.toUpperCase());
		
		Optional<Employee> emp1 = employeeRepository.findById(clearanceApprovers.getEmpId1());
		
		String immediateSupervisor = "";
		
		if(emp1.isPresent()) {
			immediateSupervisor = emp1.get().getFullName().toUpperCase();		
		}
		
//		map.put("Immediate_Supervisor", immediateSupervisor);
		map.put("Immediate_Supervisor", "");
		
		
		Optional<Employee> emp2 = employeeRepository.findById(clearanceApprovers.getEmpId2());
		
		String headOfOffice = "";
		
		if(emp2.isPresent()) {
			headOfOffice = emp2.get().getFullName().toUpperCase();		
		}
		
//		map.put("Head_Of_Office", headOfOffice);
		map.put("Head_Of_Office", "LUCH R. GEMPIS JR.");
		
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
		
		String clearingOfficer1A = "";
		
		Optional<Employee> emp3 = employeeRepository.findById(clearanceApprovers.getEmpId3());
				
		if(emp3.isPresent()) {
			clearingOfficer1A = emp3.get().getFullName().toUpperCase();		
		}
		
		String clearingOfficer1B = "";
		
		Optional<Employee> emp4 = employeeRepository.findById(clearanceApprovers.getEmpId4());
		
		if(emp4.isPresent()) {
			clearingOfficer1B = emp4.get().getFullName().toUpperCase();		
		}
		
		String clearingOfficer1C = "";
		
		Optional<Employee> emp5 = employeeRepository.findById(clearanceApprovers.getEmpId5());
		
		if(emp5.isPresent()) {
			clearingOfficer1C = emp5.get().getFullName().toUpperCase();		
		}
		
		String clearingOfficer2A = "";
		
		Optional<Employee> emp6 = employeeRepository.findById(clearanceApprovers.getEmpId6());
		
		if(emp6.isPresent()) {
			clearingOfficer2A = emp6.get().getFullName().toUpperCase();		
		}
		
		String clearingOfficer2B = "";
		
		Optional<Employee> emp7 = employeeRepository.findById(clearanceApprovers.getEmpId7());
		
		if(emp7.isPresent()) {
			clearingOfficer2B = emp7.get().getFullName().toUpperCase();		
		}
		
		String clearingOfficer3A = "";
		
		Optional<Employee> emp8 = employeeRepository.findById(clearanceApprovers.getEmpId8());
		
		if(emp8.isPresent()) {
			clearingOfficer3A = emp8.get().getFullName().toUpperCase();		
		}
		
		String clearingOfficer3B = "";
		
		Optional<Employee> emp9 = employeeRepository.findById(clearanceApprovers.getEmpId9());
		
		if(emp9.isPresent()) {
			clearingOfficer3B = emp9.get().getFullName().toUpperCase();		
		}
		
		String clearingOfficer3C = "";
		
		Optional<Employee> emp10 = employeeRepository.findById(clearanceApprovers.getEmpId10());
		
		if(emp10.isPresent()) {
			clearingOfficer3C = emp10.get().getFullName().toUpperCase();		
		}
		
		String clearingOfficer4A = "";
		
		Optional<Employee> emp11 = employeeRepository.findById(clearanceApprovers.getEmpId11());
		
		if(emp11.isPresent()) {
			clearingOfficer4A = emp11.get().getFullName().toUpperCase();		
		}
		
		String clearingOfficer5A = "";
		
		Optional<Employee> emp12 = employeeRepository.findById(clearanceApprovers.getEmpId12());
		
		if(emp12.isPresent()) {
			clearingOfficer5A = emp12.get().getFullName().toUpperCase();		
		}
		
		//1
		if(clearingOfficer1A != null && clearingOfficer1A.length() > 0) {
//			map.put("1A_Name_Clearing_Officer", clearingOfficer1A + "\n" + emp3.get().getPositionTitle().getPositionTitleName());
			map.put("1A_Name_Clearing_Officer", "RIZALINO A. ABUSMAN\nAdministrative Officer V");
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
//			map.put("1B_Name_Clearing_Officer", clearingOfficer1B + "\n" + emp4.get().getPositionTitle().getPositionTitleName());
			map.put("1B_Name_Clearing_Officer", "ROSALINDA C. MANOJO\nAdministrative Officer V");
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
//			map.put("1C_Name_Clearing_Officer", clearingOfficer1C + "\n" + emp5.get().getPositionTitle().getPositionTitleName());
			map.put("1C_Name_Clearing_Officer", "");
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
//			map.put("2A_Name_Clearing_Officer", clearingOfficer2A + "\n" + emp6.get().getPositionTitle().getPositionTitleName());
			map.put("2A_Name_Clearing_Officer", "HECTOR R. PASCUAL\nAdministrative Officer V");
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
//			map.put("2B_Name_Clearing_Officer", clearingOfficer2B + "\n" + emp7.get().getPositionTitle().getPositionTitleName());
//			map.put("2B_Cleared", "");
//			map.put("2B_Not_Cleared", "");
//			map.put("2B_Signature", "");
			map.put("2B_Name_Clearing_Officer", "N/A");
			map.put("2B_Cleared", "N/A");
			map.put("2B_Not_Cleared", "N/A");
			map.put("2B_Signature", "N/A");
		} else {
			map.put("2B_Name_Clearing_Officer", "N/A");
			map.put("2B_Cleared", "N/A");
			map.put("2B_Not_Cleared", "N/A");
			map.put("2B_Signature", "N/A");
		}
		
		//3
		if(clearingOfficer3A != null && clearingOfficer3A.length() > 0) {
//			map.put("3A_Name_Clearing_Officer", clearingOfficer3A + "\n" + emp8.get().getPositionTitle().getPositionTitleName());
//			map.put("3A_Cleared", "");
//			map.put("3A_Not_Cleared", "");
//			map.put("3A_Signature", "");
			map.put("3A_Name_Clearing_Officer", "N/A");
			map.put("3A_Cleared", "N/A");
			map.put("3A_Not_Cleared", "N/A");
			map.put("3A_Signature", "N/A");
		} else {
			map.put("3A_Name_Clearing_Officer", "N/A");
			map.put("3A_Cleared", "N/A");
			map.put("3A_Not_Cleared", "N/A");
			map.put("3A_Signature", "N/A");
		}
		
		if(clearingOfficer3B != null && clearingOfficer3B.length() > 0) {
//			map.put("3B_Name_Clearing_Officer", clearingOfficer3B + "\n" + emp9.get().getPositionTitle().getPositionTitleName());
//			map.put("3B_Cleared", "");
//			map.put("3B_Not_Cleared", "");
//			map.put("3B_Signature", "");
			map.put("3B_Name_Clearing_Officer", "N/A");
			map.put("3B_Cleared", "N/A");
			map.put("3B_Not_Cleared", "N/A");
			map.put("3B_Signature", "N/A");
		} else {
			map.put("3B_Name_Clearing_Officer", "N/A");
			map.put("3B_Cleared", "N/A");
			map.put("3B_Not_Cleared", "N/A");
			map.put("3B_Signature", "N/A");
		}
		
		if(clearingOfficer3C != null && clearingOfficer3C.length() > 0) {
//			map.put("3C_Name_Clearing_Officer", clearingOfficer3C + "\n" + emp10.get().getPositionTitle().getPositionTitleName());
			map.put("3C_Name_Clearing_Officer", "IMELDA R. GONZALES\nAdministrative Officer V");
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
//			map.put("4A_Name_Clearing_Officer", clearingOfficer4A + "\n" + emp11.get().getPositionTitle().getPositionTitleName());
			map.put("4A_Name_Clearing_Officer", "ROSALINDA C. MANOJO\nAdministrative Officer V");
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
//			map.put("IV_CONPAC_A_Name_Clearing_Officer", clearingOfficer5A + "\nChief Administrative Officer");
			map.put("IV_CONPAC_A_Name_Clearing_Officer", "CHARITO A. RUMBO\nChief Administrative Officer");
			map.put("IV_CONPAC_A_Cleared", "");
			map.put("IV_CONPAC_A_Not_Cleared", "");
			map.put("IV_CONPAC_A_Signature", "");
		} else {
			map.put("IV_CONPAC_A_Name_Clearing_Officer", "N/A");
			map.put("IV_CONPAC_A_Cleared", "N/A");
			map.put("IV_CONPAC_A_Not_Cleared", "N/A");
			map.put("IV_CONPAC_A_Signature", "N/A");
		}
		
		String emp13Str = "";
		Optional<Employee> emp13 = employeeRepository.findById(clearanceApprovers.getEmpId13());
		
		if(emp13.isPresent()) {
			emp13Str = emp13.get().getFullName().toUpperCase();		
		}
		
		
//		map.put("V_CERTIFICATION_NAME2", emp13Str);
		map.put("V_CERTIFICATION_NAME2", "LUCH R. GEMPIS JR.");
		map.put("V_CERTIFICATION_POSITION2", "City Gov't. Dept. Head III");
		
		String emp14Str = "";
		Optional<Employee> emp14 = employeeRepository.findById(clearanceApprovers.getEmpId14());
		
		if(emp14.isPresent()) {
			emp14Str = emp14.get().getFullName().toUpperCase();		
		}
		
		map.put("V_CERTIFICATION_OFFICE2", "Secretary to the City Council");
		
		
		
//		map.put("V_CERTIFICATION_NAME1", emp14Str);
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
