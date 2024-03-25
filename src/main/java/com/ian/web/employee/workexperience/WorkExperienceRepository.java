package com.ian.web.employee.workexperience;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ian.web.employee.Employee;
import com.ian.web.employee.educationalbg.EducationalBackground;


@Repository
public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {
    List<WorkExperience> findByEmployeeId(Long id);
}
