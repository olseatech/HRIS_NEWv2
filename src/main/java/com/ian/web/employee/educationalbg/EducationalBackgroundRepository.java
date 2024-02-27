package com.ian.web.employee.educationalbg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationalBackgroundRepository extends JpaRepository<EducationalBackground, Long> {
    
}
