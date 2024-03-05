package com.ian.web.employee.learning;

import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "learning_and_development")
public class LearningAndDevelopment {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@NotBlank
    private String titleOfSemimar;
	
	private String trainingCourseDesc;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateFrom;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateTo;
	
	private boolean upToPresent;

	private int noHours;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "learning_development_type", referencedColumnName = "id")
	private LearningType learningType;
	
	private String providers;
}
