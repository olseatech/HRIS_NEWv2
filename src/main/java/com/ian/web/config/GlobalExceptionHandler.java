package com.ian.web.config;

import java.util.NoSuchElementException;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.LazyInitializationException;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import com.ian.web.common.model.UXMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler to catch unhandled exceptions and prevent mid-stream rendering crashes.
 * Prevents ERR_INCOMPLETE_CHUNKED_ENCODING by handling exceptions gracefully.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * LazyInitializationException fires when a Hibernate proxy is accessed outside a
     * session (spring.jpa.open-in-view=false). If this reaches the handler the response
     * has NOT started yet — we can still send a proper error page instead of cutting the
     * stream mid-render. Controllers should pre-fetch lazy associations (see safeGet in
     * LeaveMyAccountController) so this handler is a last-resort safety net.
     */
    @ExceptionHandler(LazyInitializationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleLazyInit(LazyInitializationException ex, HttpServletRequest request) {
        log.error("LazyInitializationException on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return createErrorView("Data Load Error",
            "A required data association could not be loaded. Please refresh the page or contact IT support.",
            request);
    }

    /**
     * MailException covers all Spring Mail / JavaMail failures (SMTP unreachable, auth
     * failure, connection timeout). These must not propagate as 500s to the browser.
     */
    @ExceptionHandler(MailException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleMailException(MailException ex, HttpServletRequest request) {
        log.error("Mail delivery failure on {}: {}", request.getRequestURI(), ex.getMessage());
        return createErrorView("Notification Error",
            "Your request was processed but the notification email could not be sent. " +
            "Please check your leave status in the dashboard.",
            request);
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleNullPointerException(NullPointerException ex, HttpServletRequest request) {
        log.error("NullPointerException caught: {}", ex.getMessage(), ex);
        return createErrorView("Internal Server Error",
            "A data retrieval error occurred. Please try again or contact support.",
            request);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoSuchElement(NoSuchElementException ex, HttpServletRequest request) {
        log.error("NoSuchElementException caught: {}", ex.getMessage(), ex);
        return createErrorView("Not Found",
            "The requested resource was not found.",
            request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.error("IllegalArgumentException caught: {}", ex.getMessage(), ex);
        return createErrorView("Invalid Request",
            ex.getMessage() != null ? ex.getMessage() : "Invalid request parameters.",
            request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception caught: {}", ex.getMessage(), ex);
        return createErrorView("Application Error",
            "An unexpected error occurred. Please try again or contact your administrator.",
            request);
    }

    private ModelAndView createErrorView(String title, String message, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("error");
        view.addObject("status", 500);
        view.addObject("error", title);
        view.addObject("message", message);
        view.addObject("path", request.getRequestURI());
        return view;
    }
}
