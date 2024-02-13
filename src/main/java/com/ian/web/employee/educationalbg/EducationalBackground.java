package com.ian.web.employee.educationalbg;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class EducationalBackground {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	private boolean isDeceased;
	private String level;
	private School school;
	private DegreeCourse degree;
	
	private int startYear;
	private int startMonth;
	private int startDay;
	
	private int endYear;
	private int endMonth;
	private int endDay;
	
	private boolean upToPresent;
	private String unitsEarned;
	private int yearGraduated;
	private String scholarship;
	
	private AcademicHonors academicHonors;
	private String remarks;
	
}
