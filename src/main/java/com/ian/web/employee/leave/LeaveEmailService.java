package com.ian.web.employee.leave;

import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends email notifications to employees and HR admins when a leave application
 * changes status. All sends are async (non-blocking) and logged to
 * leave_notification_log for audit and retry.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveEmailService {

    private final JavaMailSender mailSender;
    private final LeaveNotificationLogRepository notifLogRepo;

    @Value("${spring.mail.username:hris-noreply@agency.gov.ph}")
    private String fromAddress;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    // -----------------------------------------------------------------------
    // Public trigger methods — called by LeaveService after each status change
    // -----------------------------------------------------------------------

    @Async
    public void notifyApplied(LeaveApplication app) {
        String subject = "[Leave] Application #" + app.getId() + " Submitted";
        String body = buildBody(app, "Your leave application has been submitted and is pending HR review.", null);
        sendToEmployee(app, "APPLIED", subject, body);
    }

    @Async
    public void notifyEndorsed(LeaveApplication app) {
        String subject = "[Leave] Application #" + app.getId() + " Endorsed";
        String body = buildBody(app, "Your leave application has been endorsed by HR and forwarded for final approval.", null);
        sendToEmployee(app, "ENDORSED", subject, body);
    }

    @Async
    public void notifyApproved(LeaveApplication app) {
        String subject = "[Leave] Application #" + app.getId() + " Approved";
        String payLabel = app.isWithPay() ? "with pay" : "without pay (LWOP)";
        String extra = "Your leave has been approved " + payLabel + ".";
        if (app.getFinalDaysWithPay() != null) {
            extra += "\n  Days with pay    : " + app.getFinalDaysWithPay();
            extra += "\n  Days without pay : " + app.getFinalDaysWithoutPay();
        }
        String body = buildBody(app, extra, app.getRemarks());
        sendToEmployee(app, "APPROVED", subject, body);
    }

    @Async
    public void notifyDisapproved(LeaveApplication app) {
        String subject = "[Leave] Application #" + app.getId() + " Disapproved";
        String body = buildBody(app, "Your leave application has been disapproved.", app.getRemarks());
        sendToEmployee(app, "DISAPPROVED", subject, body);
    }

    @Async
    public void notifyReturned(LeaveApplication app) {
        String subject = "[Leave] Application #" + app.getId() + " Returned for Correction";
        String body = buildBody(app,
                "Your leave application has been returned for correction. Please log in to review and resubmit.",
                app.getRemarks());
        sendToEmployee(app, "RETURNED", subject, body);
    }

    @Async
    public void notifyCancelled(LeaveApplication app) {
        String subject = "[Leave] Application #" + app.getId() + " Cancelled";
        String body = buildBody(app, "Your leave application has been cancelled.", app.getCancelReason());
        sendToEmployee(app, "CANCELLED", subject, body);
    }

    @Async
    public void notifyCancellationRequested(LeaveApplication app) {
        String subject = "[Leave] Cancellation Request #" + app.getId() + " Submitted";
        String body = buildBody(app,
            "Your request to cancel this approved leave has been submitted and is pending review "
            + "by the Head of Agency. You will be notified once a decision has been made.", null);
        sendToEmployee(app, "CANCEL_REQUESTED", subject, body);
    }

    @Async
    public void notifyCancellationHrAcknowledged(LeaveApplication app) {
        String subject = "[Leave] Cancellation Request #" + app.getId() + " Acknowledged by HR";
        String body = buildBody(app,
            "Your Travel Leave cancellation request has been acknowledged by HR and forwarded "
            + "to the Head of Agency for final decision. You will be notified once a decision "
            + "has been made.", null);
        sendToEmployee(app, "CANCELLATION_HR_ACKNOWLEDGED", subject, body);
    }

    @Async
    public void notifyCancellationApproved(LeaveApplication app) {
        String subject = "[Leave] Cancellation Request #" + app.getId() + " Approved";
        boolean restored = app.isLedgerPosted() && app.isWithPay()
                && app.getDateFrom() != null && app.getDateFrom().isAfter(java.time.LocalDate.now());
        String detail = restored
            ? "Your cancellation has been approved and your leave credits have been restored."
            : "Your cancellation has been approved. Leave credits were not restored because the "
              + "leave had already started.";
        String body = buildBody(app, detail, app.getCancelReason());
        sendToEmployee(app, "CANCELLATION_APPROVED", subject, body);
    }

    @Async
    public void notifyCancellationRejected(LeaveApplication app) {
        String subject = "[Leave] Cancellation Request #" + app.getId() + " Rejected";
        String body = buildBody(app,
            "Your request to cancel this approved leave has been rejected. "
            + "The application remains APPROVED. Please contact HR if you have questions.", null);
        sendToEmployee(app, "CANCELLATION_REJECTED", subject, body);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void sendToEmployee(LeaveApplication app, String eventType, String subject, String body) {
        String email = app.getEmployee().getEmail1();
        if (email == null || email.isBlank()) {
            log.debug("No email address for employee {}; skipping {} notification for application #{}",
                    app.getEmployee().getId(), eventType, app.getId());
            return;
        }

        LeaveNotificationLog log_ = new LeaveNotificationLog();
        log_.setApplication(app);
        log_.setRecipientEmail(email);
        log_.setRecipientName(app.getEmployee().getDisplayName());
        log_.setEventType(eventType);
        log_.setStatus("PENDING");

        LeaveNotificationLog saved;
        try {
            saved = notifLogRepo.save(log_);
        } catch (Exception e) {
            log.warn("Could not persist notification log for application #{}: {}",
                    app.getId(), e.getMessage());
            // Still attempt to send email even if log persistence fails
            saved = log_;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(email);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);

            saved.setStatus("SENT");
            saved.setSentAt(java.time.LocalDateTime.now());
            if (saved.getId() != null) notifLogRepo.save(saved);
            log.info("Leave notification sent: {} to {} for application #{}",
                    eventType, email, app.getId());
        } catch (Exception e) {
            saved.setStatus("FAILED");
            saved.setErrorMessage(e.getMessage());
            if (saved.getId() != null) notifLogRepo.save(saved);
            log.warn("Failed to send leave notification {} to {} for application #{}: {}",
                    eventType, email, app.getId(), e.getMessage());
        }
    }

    private String buildBody(LeaveApplication app, String mainMessage, String remarks) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(app.getEmployee().getDisplayName()).append(",\n\n");
        sb.append(mainMessage).append("\n\n");
        sb.append("Application Details\n");
        sb.append("-------------------\n");
        sb.append("Application #  : ").append(app.getId()).append("\n");
        sb.append("Leave Type     : ").append(app.getLeaveType().getLeaveName()).append("\n");
        sb.append("Date From      : ").append(app.getDateFrom().format(FMT)).append("\n");
        sb.append("Date To        : ").append(app.getDateTo().format(FMT)).append("\n");
        sb.append("Working Days   : ").append(app.getNumberOfDays()).append("\n");
        sb.append("Status         : ").append(app.getStatus()).append("\n");
        if (remarks != null && !remarks.isBlank()) {
            sb.append("Remarks        : ").append(remarks).append("\n");
        }
        sb.append("\nPlease log in to the HRIS portal to view your leave record.\n\n");
        sb.append("This is an automated message. Do not reply to this email.\n");
        sb.append("Government HRIS — Human Resource Information System");
        return sb.toString();
    }
}
