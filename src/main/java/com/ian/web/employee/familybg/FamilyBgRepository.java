package com.ian.web.employee.familybg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyBgRepository  extends JpaRepository<FamilyBg, Long> {
	
	List<FamilyBg> findByEmployeeId(long employeeId);
	FamilyBg findByEmployeeIdAndRelationship(long employeeId, String relationship);

}
