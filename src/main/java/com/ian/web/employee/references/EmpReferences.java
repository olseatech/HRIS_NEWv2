package com.ian.web.employee.references;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class EmpReferences {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	private String referenceName;
	private String positionTitle;
	private String companyAddress;
	private String companyContactNo;

}
