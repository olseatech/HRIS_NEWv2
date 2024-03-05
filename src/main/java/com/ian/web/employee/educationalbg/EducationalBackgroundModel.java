package com.ian.web.employee.educationalbg;

import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.springframework.format.annotation.DateTimeFormat;

import com.ian.web.systemsettings.academichonors.AcademicHonors;
import com.ian.web.systemsettings.degree_courses.DegreeCourses;
import com.ian.web.systemsettings.degreelevels.DegreeLevel;
import com.ian.web.systemsettings.scholarship.Scholarship;
import com.ian.web.systemsettings.schools.School;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
