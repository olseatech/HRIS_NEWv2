package com.ian.web.employee.govermentid;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ian.web.employee.Employee;

@Repository
public interface GovermentIssuedIdRepository extends JpaRepository<GovermentIssuedId, Long>{
    List<GovermentIssuedId> findAllByEmployee(Employee employee);
}
