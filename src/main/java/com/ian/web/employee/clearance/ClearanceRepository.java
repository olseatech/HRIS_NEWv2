package com.ian.web.employee.clearance;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClearanceRepository extends JpaRepository<Clearance, Long> {

	List<Clearance> findByEmployeeId(long employeeId);
	List<Clearance> findByStatus(String status);
	List<Clearance> findByPurpose(String purpose);
	
}
