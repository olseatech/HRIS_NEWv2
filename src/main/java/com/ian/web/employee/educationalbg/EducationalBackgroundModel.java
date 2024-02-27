package com.ian.web.employee.educationalbg;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

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
	private Long degreeLevelId;
	private Long schoolId;
	private Long degreeCourseId;

	private int startYear;
	private int startMonth;
	private int startDay;
	
	private int endYear;
	private int endMonth;
	private int endDay;
	
	private boolean upToPresent;

	private String unitsEarned;
	private int yearGraduated;
	private Long scholarshipId;
	private Long academicHonorsId;
	private String remarks;
}
