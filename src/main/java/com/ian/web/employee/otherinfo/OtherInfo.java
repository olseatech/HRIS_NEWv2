package com.ian.web.employee.otherinfo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class OtherInfo {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	private String specialSkill;
	private String nonAcademic;
	private String membershipInAssociation;

}
