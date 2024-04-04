package com.ian.web.employee.clearance;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.ian.web.employee.Employee;

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
	
	private long empId1;
	private long empId2;
	private long empId3;
	private long empId4;
	private long empId5;
	private long empId6;
	private long empId7;
	private long empId8;
	private long empId9;
	private long empId10;
	private long empId11;
	private long empId12;
	private long empId13;
	private long empId14;

}
