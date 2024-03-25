package com.ian.web.employee.educationalbg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ian.web.employee.Employee;
import com.ian.web.employee.familybg.FamilyBg;


@Repository
public interface EducationalBackgroundRepository extends JpaRepository<EducationalBackground, Long> {
//    List<EducationalBackground> findAllByEmployee(Employee employee);
    
    List<EducationalBackground> findByEmployeeId(long employeeId);
//    EducationalBackground findByEmployeeIdAndRelationship(long employeeId, String relationship);
}
