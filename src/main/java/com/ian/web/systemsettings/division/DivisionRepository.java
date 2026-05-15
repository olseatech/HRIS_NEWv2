package com.ian.web.systemsettings.division;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Long> {

    Optional<Division> findByDivisionName(String divisionName);

    /**
     * Returns all divisions that have at least one approver configured,
     * with both approver1 and approver2 JOIN FETCHed to avoid lazy-loading
     * in the controller layer.
     * Used to build the "Forward To Division Head" dropdown.
     */
    @Query("SELECT DISTINCT d FROM Division d " +
           "LEFT JOIN FETCH d.approver1 " +
           "LEFT JOIN FETCH d.approver2 " +
           "WHERE d.approver1 IS NOT NULL OR d.approver2 IS NOT NULL")
    List<Division> findAllWithApprovers();
}
