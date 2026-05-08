package com.ian.web.employee;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface EmployeeRepository  extends JpaRepository<Employee, Long> {
	Optional<Employee> findByPlantillaNo(String plantillaNo);
	List<Employee> findByFirstNameAndLastName(String firstName, String lastName);
	Optional<Employee> findByEmpNo(String empNo);
	Optional<Employee> findByUsername(String username);

    /**
     * Security login query — returns the employee with ALL four EAGER associations
     * fully JOIN-FETCHed so the stored session.actorObj never holds uninitialized
     * Hibernate proxies. Without this, any template that touches division, positionTitle,
     * district, or employeeStatus on the detached session entity throws
     * LazyInitializationException mid-render → ERR_INCOMPLETE_CHUNKED_ENCODING.
     */
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.division " +
           "LEFT JOIN FETCH e.positionTitle " +
           "LEFT JOIN FETCH e.district " +
           "LEFT JOIN FETCH e.employeeStatus " +
           "WHERE e.username = :username")
    Optional<Employee> findByUsernameFetched(
            @org.springframework.data.repository.query.Param("username") String username);
	Optional<Employee> findByIdAndEmpHashCode(long id, String empHashCode);
	Optional<Employee> findById(long id);
	List<Employee> findByEmpNoOrPlantillaNo(String empNo, String plantillaNo);

	List<Employee> findByFirstNameAndLastNameAndBirthdate(String firstName, String lastName, LocalDate birthDate);
	
//	@Query("SELECT e.employeeStatus, COUNT(e) FROM Employee e GROUP BY e.employeeStatus")
//	Map<Long, Long> countEmployeeStatus();
	
	@Query(value = "SELECT division_id, COUNT(*) FROM employee GROUP BY division_id", nativeQuery = true)
	List<Object[]> countEmployeeDivision();
	
	@Query(value = "SELECT employee_status_id, COUNT(*) FROM employee GROUP BY employee_status_id", nativeQuery = true)
	List<Object[]> countEmployeeStatus();
	
	@Query("SELECT e FROM Employee e WHERE MONTH(e.birthdate) = MONTH(CURRENT_DATE)")
    List<Employee> findEmployeesWithBirthMonth();

    /**
     * Returns all employees with their associations fully initialised in one query.
     * Use this instead of findAll() anywhere the template or service accesses
     * employee.division, positionTitle, district, or employeeStatus — those are
     * declared EAGER on the entity, but JPQL (findAll) does NOT auto-JOIN-FETCH them.
     * Without this, Hibernate issues N+1 secondary SELECTs and, if the session is
     * already closed (spring.jpa.open-in-view=false), throws LazyInitializationException
     * mid-render, causing ERR_INCOMPLETE_CHUNKED_ENCODING.
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "LEFT JOIN FETCH e.division " +
           "LEFT JOIN FETCH e.positionTitle " +
           "LEFT JOIN FETCH e.district " +
           "LEFT JOIN FETCH e.employeeStatus " +
           "ORDER BY e.lastName ASC, e.firstName ASC")
    List<Employee> findAllWithAssociationsFetched();

    /**
     * Single-employee lookup with all four EAGER associations pre-fetched.
     * Use this instead of findById() wherever a template or service accesses
     * employee.division, positionTitle, district, or employeeStatus, so that
     * Hibernate never issues N+1 secondaries and there is no risk of a
     * LazyInitializationException if the OSIV session boundary shifts.
     */
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.division " +
           "LEFT JOIN FETCH e.positionTitle " +
           "LEFT JOIN FETCH e.district " +
           "LEFT JOIN FETCH e.employeeStatus " +
           "WHERE e.id = :id")
    Optional<Employee> findByIdFetched(@org.springframework.data.repository.query.Param("id") Long id);

    /**
     * Single-employee lookup with ID and hash code (for security), with all four EAGER
     * associations pre-fetched. Use this instead of findByIdAndEmpHashCode() when
     * rendering templates that access employee.division, positionTitle, district, or
     * employeeStatus, to prevent N+1 queries and LazyInitializationException.
     */
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.division " +
           "LEFT JOIN FETCH e.positionTitle " +
           "LEFT JOIN FETCH e.district " +
           "LEFT JOIN FETCH e.employeeStatus " +
           "WHERE e.id = :id AND e.empHashCode = :empHashCode")
    Optional<Employee> findByIdAndEmpHashCodeFetched(
            @org.springframework.data.repository.query.Param("id") long id,
            @org.springframework.data.repository.query.Param("empHashCode") String empHashCode);

    default Map<Long, Long> getCountEmployeeStatus() {
        List<Object[]> result = countEmployeeStatus();
        Map<Long, Long> statusCounts = new HashMap<>();
        for (Object[] row : result) {
            //statusCounts.put((Long) row[0], (Long) row[1]);
        	BigInteger statusIdBigInt = (BigInteger) row[0];
            BigInteger countBigInt = (BigInteger) row[1];

            // Convert BigInteger to Long
            Long statusId = statusIdBigInt.longValue();
            Long count = countBigInt.longValue();

            // Now, you can put the values into the map
            statusCounts.put(statusId, count);
        }
        return statusCounts;
    }
    
    default Map<Long, Long> getCountEmployeeDivision() {
        List<Object[]> result = countEmployeeDivision();
        Map<Long, Long> statusCounts = new HashMap<>();
        for (Object[] row : result) {
            //statusCounts.put((Long) row[0], (Long) row[1]);
        	BigInteger statusIdBigInt = (BigInteger) row[0];
            BigInteger countBigInt = (BigInteger) row[1];

            // Convert BigInteger to Long
            Long statusId = statusIdBigInt.longValue();
            Long count = countBigInt.longValue();

            // Now, you can put the values into the map
            statusCounts.put(statusId, count);
        }
        return statusCounts;
    }
}
