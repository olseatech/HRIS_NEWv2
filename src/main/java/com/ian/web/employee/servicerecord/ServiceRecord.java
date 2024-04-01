package com.ian.web.employee.servicerecord;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.springframework.format.annotation.DateTimeFormat;

import com.ian.web.employee.Employee;
import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor 
@AllArgsConstructor
public class ServiceRecord {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateFrom;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateTo;
		
    private String isPresent;
    
    private String designation;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_status_id")
    private EmployeeStatus employeeStatus;
    
    @ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	Employee employee;
    
    private Double salary;
    
    private String station;
    
    private String branch;
    
    private String lvAbs;
    
    private String separationCause;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate separationDate;
    
    @Transient
    private String showMode;

}
