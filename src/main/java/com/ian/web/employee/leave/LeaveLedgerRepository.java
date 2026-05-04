package com.ian.web.employee.leave;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveLedgerRepository extends JpaRepository<LeaveLedger, Long> {

    List<LeaveLedger> findByEmployeeIdOrderByTransactionDateDesc(Long employeeId);

    List<LeaveLedger> findByEmployeeIdAndLeaveTypeIdOrderByTransactionDateDesc(Long employeeId, Long leaveTypeId);

    @Query("SELECT l FROM LeaveLedger l WHERE l.employee.id = :empId AND l.reference = :ref")
    List<LeaveLedger> findByEmployeeIdAndReference(@Param("empId") Long employeeId, @Param("ref") String reference);
}
