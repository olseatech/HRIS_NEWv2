package com.ian.web.employee.voluntary_workexperience;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ian.web.employee.Employee;

@Repository
public interface VoluntaryWorkRepository extends JpaRepository<VoluntaryWork, Long>{
    List<VoluntaryWork> findByEmployeeId(Long id);
}
