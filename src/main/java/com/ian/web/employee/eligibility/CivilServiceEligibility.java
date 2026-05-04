package com.ian.web.employee.eligibility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.ian.web.employee.Employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "civil_service_eligibility")
public class CivilServiceEligibility {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    private String eligibility;
	private String otherEligibility;
	private String rating;
	
	private int examYear;
	private String examMonth;
	private int examDay;
	private String placeOfExam;
	
	private String licenseNo;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate licenseValidityDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate licenseReleaseDate;
	
	private String attachmentUrl;
	
	@Transient
	private MultipartFile attachedFile;

	@Transient
	private String showMode;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	private Employee employee;
	
	@Transient
	public String getExamDate(){
        return examMonth+" "+examYear;
	}
}
