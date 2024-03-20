package com.ian.web.employee.learning;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ian.web.employee.Employee;

@Repository
public interface LearningAndDevelopmentRepository extends JpaRepository<LearningAndDevelopment, Long> {
    List<LearningAndDevelopment> findAllByEmployee(Employee employee);
}
