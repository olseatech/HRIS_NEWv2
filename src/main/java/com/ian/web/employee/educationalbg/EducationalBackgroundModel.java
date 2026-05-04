package com.ian.web.employee.educationalbg;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class EducationalBackgroundModel {
    private Long id;
	private Long degreeLevelId = 0L ;
	private Long schoolId = 0L;
	private Long degreeCourseId = 0L;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate; 
	private boolean upToPresent;

	private String unitsEarned;
	private int yearGraduated;
	private Long scholarshipId = 0L;
	private Long academicHonorsId = 0L;
	private String remarks;
}
