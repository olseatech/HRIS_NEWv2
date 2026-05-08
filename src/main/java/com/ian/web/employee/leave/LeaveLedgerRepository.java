package com.ian.web.employee.leave;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveLedgerRepository extends JpaRepository<LeaveLedger, Long> {

    List<LeaveLedger> findByEmployeeIdOrderByTransactionDateDesc(Long employeeId);

    /**
     * Same as findByEmployeeIdOrderByTransactionDateDesc but with leaveType JOIN-FETCHed.
     * Use this for templates that access ledger.leaveType.leaveName to avoid
     * LazyInitializationException when spring.jpa.open-in-view=false.
     */
    @Query("SELECT l FROM LeaveLedger l " +
           "JOIN FETCH l.leaveType " +
           "WHERE l.employee.id = :empId " +
           "ORDER BY l.transactionDate DESC")
    List<LeaveLedger> findByEmployeeIdFetched(@Param("empId") Long employeeId);

    List<LeaveLedger> findByEmployeeIdAndLeaveTypeIdOrderByTransactionDateDesc(Long employeeId, Long leaveTypeId);

    @Query("SELECT l FROM LeaveLedger l WHERE l.employee.id = :empId AND l.reference = :ref")
    List<LeaveLedger> findByEmployeeIdAndReference(@Param("empId") Long employeeId, @Param("ref") String reference);
}
