package com.ian.web.employee.learning;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningAndDevelopmentRepository extends JpaRepository<LearningAndDevelopment, Long> {
    List<LearningAndDevelopment> findByEmployeeId(Long id);
}
