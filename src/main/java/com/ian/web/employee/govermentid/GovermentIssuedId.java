package com.ian.web.employee.govermentid;

import java.time.LocalDate;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.format.annotation.DateTimeFormat;

public class GovermentIssuedId {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	private String govermentIssuedId;
	private String idNo;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate issuanceDate;
	
	private String placeOfIssuance;

}
