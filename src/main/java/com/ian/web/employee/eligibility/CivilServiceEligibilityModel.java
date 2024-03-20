package com.ian.web.employee.eligibility;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;

import com.ian.web.systemsettings.eligibility.Eligibility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CivilServiceEligibilityModel {
    private Long eligibilityId;
	private String otherEligibility;
	private String rating;
	
	private Integer examYear;
	private Integer examMonth;
	private Integer examDay;
	private String placeOfExam;
	
	private String licenseNo;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate licenseValidityDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate licenseReleaseDate;

	private String attachmentUrl;
}
