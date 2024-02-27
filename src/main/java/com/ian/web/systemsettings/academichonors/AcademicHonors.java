package com.ian.web.systemsettings.academichonors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ian.web.employee.educationalbg.EducationalBackground;

import lombok.Data;

@Entity
@Data
@Table(name = "academic_honors")
public class AcademicHonors {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	@NotBlank(message = " is mandatory.")
    private String academicHonorsName;	

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "academicHonors")
    @JsonIgnore
    private EducationalBackground educationalBackground;
}
