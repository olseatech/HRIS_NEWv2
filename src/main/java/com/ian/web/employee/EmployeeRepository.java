package com.ian.web.employee;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository  extends JpaRepository<Employee, Long> {
	Optional<Employee> findByUsername(String username);
	Optional<Employee> findByIdAndEmpHashCode(long id, String empHashCode);
	Optional<Employee> findById(long id);

	List<Employee> findByFirstNameAndLastNameAndBirthdate(String firstName, String lastName, LocalDate birthDate);
}
