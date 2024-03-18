package com.ian.web.employee.otherinfo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ian.web.employee.Employee;

@Repository
public interface OtherInfoRepository extends JpaRepository<OtherInfo, Long> {
    List<OtherInfo> findAllByEmployee(Employee employee);
}
