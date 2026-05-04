package com.ian.web.employee.servicerecord;

import lombok.Data;

@Data
public class ServiceRecordReportDto {
	
	private String dateFrom;
	private String dateTo;
	private String designation;
	private String employeeStatus;
	private String salary;    
    private String station;    
    private String branch;    
    private String lvAbs;    
    private String separationCause;
    private String separationDate;

}
