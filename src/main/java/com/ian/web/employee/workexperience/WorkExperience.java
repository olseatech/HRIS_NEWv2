package com.ian.web.employee.workexperience;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;

public class WorkExperience {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateFrom;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateTo;
	
	private boolean upToPresent;
	
	@NotBlank
    private String positionTitle;
	
	private String department;
	private String officeName;
	private String immediateSupervisor;
	private String jobDescription;
	
	private BigDecimal salary;
	private int salaryGrade;
	private int stepNo;
	private String appointmentStatus;
	private String govtOffice;
	
	private String remarks;
	
	

}
