package com.ian.web.employee.servicerecord;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRecordRepository  extends JpaRepository<ServiceRecord, Long> {
	
	List<ServiceRecord> findByEmployeeId(long employeeId);

}
