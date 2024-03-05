package com.ian.web.employee.voluntary_workexperience;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoluntaryWorkRepository extends JpaRepository<VoluntaryWork, Long>{
    
}
