package com.ian.web.employee.eligibility;

import java.time.LocalDate;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

public class CivilServiceEligibility {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	@NotBlank
    private String eligibilityName;
	private String othersEligibility;
	
	private String rating;
	
	private int examYear;
	private int examMonth;
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
	
}
