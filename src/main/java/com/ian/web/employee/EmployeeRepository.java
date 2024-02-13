package com.ian.web.employee;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource
public interface EmployeeRepository  extends JpaRepository<Employee, Long> {
	Optional<Employee> findByUsername(String username);
	Optional<Employee> findByIdAndEmpHashCode(long id, String empHashCode);
}
