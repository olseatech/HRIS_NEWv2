package com.ian.web.employee.leave;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveStatusHistoryRepository extends JpaRepository<LeaveStatusHistory, Long> {

    /** Full history for a specific leave application, ordered oldest → newest. */
    List<LeaveStatusHistory> findByApplicationIdOrderByChangedAtAsc(Long applicationId);

    /** All history entries for a specific actor (useful for audit/admin reports). */
    List<LeaveStatusHistory> findByActorIdOrderByChangedAtDesc(Long actorId);
}
