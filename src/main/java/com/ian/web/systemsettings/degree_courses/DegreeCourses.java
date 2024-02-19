package com.ian.web.systemsettings.degree_courses;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "degree_courses")
@Entity
public class DegreeCourses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = " is mandatory.")
    private String degreeCourse;
    @NotBlank(message = " is mandatory.")
    private String abbreviation;
    private boolean isLawDegree;
    @Builder.Default
    private boolean isActive = true;
}
