package com.ian.web.employee.eligibility;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ian.web.employee.Employee;

@Repository
public interface CivilServiceEligibilityRepository extends JpaRepository<CivilServiceEligibility, Long> {
    List<CivilServiceEligibility> findByEmployeeId(Long id);
}
