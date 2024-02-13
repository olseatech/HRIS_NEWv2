package com.ian.web.employee.workexperience;

import java.time.LocalDate;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;

public class VoluntaryWork {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	@NotBlank
    private String orgName;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateFrom;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateTo;
	
	private boolean upToPresent;
	
	private int noHours;
	
	private String natureOfWork;
	
	private String address;

}
