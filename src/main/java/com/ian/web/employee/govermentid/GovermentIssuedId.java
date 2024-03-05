package com.ian.web.employee.govermentid;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "government_issued_id")
@Entity
public class GovermentIssuedId {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	private String govermentIssuedId;
	private String idNo;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate issuanceDate;
	
	private String placeOfIssuance;

}
