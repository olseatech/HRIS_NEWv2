package com.ian.web.employee.eligibility;

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
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.ian.web.systemsettings.eligibility.Eligibility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "civil_service_eligibility")
public class CivilServiceEligibility {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@NonNull
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "fk_civil_service_eligibility", referencedColumnName = "id")
    private Eligibility eligibility;
	@NotBlank
	private String otherEligibility;
	private String rating;
	
	private int examYear;
	private int examMonth;
	private int examDay;
	private String placeOfExam;
	
	private String licenseNo;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate licenseValidityDate;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate licenseReleaseDate;
	private String attachmentUrl;
	
	@Transient
	private MultipartFile attachedFile;
	
}
