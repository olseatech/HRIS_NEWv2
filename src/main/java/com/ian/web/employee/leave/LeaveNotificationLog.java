package com.ian.web.employee.leave;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.*;

/**
 * Audit log of every email/notification sent for leave application events.
 * Allows retrying FAILED notifications and auditing delivery.
 */
@Entity
@Table(name = "leave_notification_log",
       indexes = {
           @Index(name = "idx_notif_app",    columnList = "application_id"),
           @Index(name = "idx_notif_status", columnList = "status")
       })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveNotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LeaveApplication application;

    @Column(nullable = false, length = 255)
    private String recipientEmail;

    @Column(length = 255)
    private String recipientName;

    /**
     * The event that triggered this notification.
     * APPLIED | ENDORSED | APPROVED | DISAPPROVED | RETURNED | CANCELLED
     */
    @Column(nullable = false, length = 50)
    private String eventType;

    private LocalDateTime sentAt;

    /**
     * PENDING | SENT | FAILED
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
