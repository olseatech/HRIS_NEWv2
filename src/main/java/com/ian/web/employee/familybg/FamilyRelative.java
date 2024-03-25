package com.ian.web.employee.familybg;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.ian.web.common.model.Person;


public class FamilyRelative extends Person {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	private boolean isDeceased;
	private String tin;
	private String occupation;
	private String employer;
	private String telNo;
	private String businessAdd;
	private String employmentStatus;
	private String mobileNo;
	
//	@ManyToOne
//    @JoinColumn(name = "family_bg_id")
//    private FamilyBg familyBg;
	
}
