package com.ian.web.employee.eligibility;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CivilServiceEligibilityRepository extends JpaRepository<CivilServiceEligibility, Long> {
    
}
