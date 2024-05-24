package com.ian.web.employee.servicerecord;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRecordReportRequestRepository  extends JpaRepository<ServiceRecordReportRequest, Long> {
	
	List<ServiceRecordReportRequest> findByEmployeeId(long employeeId);

}
