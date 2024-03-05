package com.ian.web.employee.learning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningAndDevelopmentRepository extends JpaRepository<LearningAndDevelopment, Long> {
    
}
