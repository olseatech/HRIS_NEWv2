package com.ian.web.employee.educationalbg;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.ian.web.systemsettings.academichonors.AcademicHonors;
import com.ian.web.systemsettings.degree_courses.DegreeCourses;
import com.ian.web.systemsettings.degreelevels.DegreeLevel;
import com.ian.web.systemsettings.scholarship.Scholarship;
import com.ian.web.systemsettings.schools.School;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "educational_background")
@Entity
public class EducationalBackground {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "education_background_degree_level", referencedColumnName = "id")
	private DegreeLevel degreeLevel;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "educational_background_school", referencedColumnName = "id")
	private School school;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "educational_background_degree_course", referencedColumnName = "id")
	private DegreeCourses degreeCourse;
	
	private int startYear;
	private int startMonth;
	private int startDay;
	
	private int endYear;
	private int endMonth;
	private int endDay;
	
	private boolean upToPresent;

	private String unitsEarned;
	private int yearGraduated;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "educational_background_scholarship", referencedColumnName = "id")
	private Scholarship scholarship;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "educational_background_academic_honors", referencedColumnName = "id")
	private AcademicHonors academicHonors;
	private String remarks;
	
}
