package com.ian.web.employee.approvers;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor 
@AllArgsConstructor
public class ClearanceApprovers {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private String immediateSupervisor;
	private String headOfOffice;
	
	private String adminPersonA;
	private String adminPersonB;
	private String adminPersonC;
	private String adminPositionA;
	private String adminPositionB;
	private String adminPositionC;
	
	private String libraryPersonA;
	private String libraryPersonB;
	private String libraryPositionA;
	private String libraryPositionB;
	
	private String financePersonA;
	private String financePersonB;
	private String financePersonC;
	private String financePositionA;
	private String financePositionB;
	private String financePositionC;
	
	private String professionalPersonA;
	private String professionalPositionA;
	
	private String section4Person;
	private String section4Position;
	
	private String footerPerson1;
	private String footerPerson2;
	private String footerPosition1;
	private String footerPosition2;

}
