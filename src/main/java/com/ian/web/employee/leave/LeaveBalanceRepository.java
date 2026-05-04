package com.ian.web.employee.leave;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    List<LeaveBalance> findByEmployeeIdAndBalanceYear(Long employeeId, int year);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndBalanceYear(Long employeeId, Long leaveTypeId, int year);

    List<LeaveBalance> findByEmployeeIdOrderByBalanceYearDescLeaveTypeLeaveNameAsc(Long employeeId);

    List<LeaveBalance> findByLeaveTypeIdAndBalanceYear(Long leaveTypeId, int year);
}
