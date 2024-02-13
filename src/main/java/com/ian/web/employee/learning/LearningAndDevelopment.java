package com.ian.web.employee.learning;

import java.time.LocalDate;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;

public class LearningAndDevelopment {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	@NotBlank
    private String titleOfSemimar;
	
	private String trainingCourseDesc;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateFrom;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateTo;
	
	private boolean upToPresent;

	private int noHours;
	
	private LearningType learningType;
	
	private String providers;
}
