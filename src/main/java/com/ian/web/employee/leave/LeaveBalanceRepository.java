package com.ian.web.employee.leave;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    List<LeaveBalance> findByEmployeeIdAndBalanceYear(Long employeeId, int year);

    @org.springframework.data.jpa.repository.Query(
        "SELECT DISTINCT b FROM LeaveBalance b " +
        "JOIN FETCH b.leaveType " +
        "JOIN FETCH b.employee e " +
        "LEFT JOIN FETCH e.division " +
        "LEFT JOIN FETCH e.positionTitle " +
        "LEFT JOIN FETCH e.district " +
        "LEFT JOIN FETCH e.employeeStatus " +
        "WHERE e.id = :empId AND b.balanceYear = :year " +
        "ORDER BY b.leaveType.leaveName ASC")
    List<LeaveBalance> findByEmployeeIdAndYearFetched(
        @org.springframework.data.repository.query.Param("empId") Long employeeId,
        @org.springframework.data.repository.query.Param("year") int year);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndBalanceYear(Long employeeId, Long leaveTypeId, int year);

    List<LeaveBalance> findByEmployeeIdOrderByBalanceYearDescLeaveTypeLeaveNameAsc(Long employeeId);

    List<LeaveBalance> findByLeaveTypeIdAndBalanceYear(Long leaveTypeId, int year);
}
