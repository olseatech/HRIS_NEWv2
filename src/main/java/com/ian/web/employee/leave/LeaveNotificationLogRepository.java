package com.ian.web.employee.leave;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveNotificationLogRepository extends JpaRepository<LeaveNotificationLog, Long> {

    List<LeaveNotificationLog> findByStatusOrderByCreatedAtAsc(String status);

    List<LeaveNotificationLog> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
