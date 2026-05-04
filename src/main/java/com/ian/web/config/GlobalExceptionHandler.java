package com.ian.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.thymeleaf.exceptions.TemplateProcessingException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TemplateProcessingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleTemplateError(TemplateProcessingException ex,
                                      HttpServletRequest request,
                                      Model model) {
        log.error("Thymeleaf rendering failed for [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorTitle", "Page Rendering Error");
        model.addAttribute("errorMessage",
                "The page could not be displayed due to a data error. " +
                "Please go back and try again, or contact IT support if the issue persists.");
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/general-error";
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleNullPointer(NullPointerException ex,
                                    HttpServletRequest request,
                                    Model model) {
        log.error("NullPointerException on [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorTitle", "Unexpected Error");
        model.addAttribute("errorMessage",
                "An unexpected error occurred while loading this page. " +
                "Please try again or contact IT support.");
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/general-error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex,
                                HttpServletRequest request,
                                Model model) {
        log.error("Unhandled exception on [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorTitle", "Server Error");
        model.addAttribute("errorMessage",
                "An error occurred on the server. Please try again later.");
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/general-error";
    }
}
