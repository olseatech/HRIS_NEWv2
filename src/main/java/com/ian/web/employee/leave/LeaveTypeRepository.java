package com.ian.web.employee.leave;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    List<LeaveType> findAllByOrderBySortOrderAscLeaveNameAsc();

    List<LeaveType> findByActiveTrueOrderBySortOrderAscLeaveNameAsc();

    boolean existsByLeaveCode(String leaveCode);

    boolean existsByLeaveCodeAndIdNot(String leaveCode, Long id);

    Optional<LeaveType> findByLeaveCode(String leaveCode);
}
