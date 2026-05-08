package com.ian.web.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdsCountDto {	
	
	private int familyBgCount;	
	private int educationalBgCount;	
	private int eligibilityCount;	
	private int workExperienceCount;	
	private int voluntaryWorkCount;
	private int learningDevCount;
	private int otherInfoCount;
	private int otherInfoQuestionsCount;
	private int referencesCount;
	private int govIdCount;

}
