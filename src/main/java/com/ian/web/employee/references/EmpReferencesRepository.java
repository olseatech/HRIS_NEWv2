package com.ian.web.employee.references;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ian.web.employee.Employee;

@Repository
public interface EmpReferencesRepository extends JpaRepository<EmpReferences, Long> {
    List<EmpReferences> findAllByEmployee(Employee employee);
}
