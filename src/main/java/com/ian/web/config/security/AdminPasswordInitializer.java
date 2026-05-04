package com.ian.web.config.security;

import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class AdminPasswordInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder    passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            Employee admin = employeeRepository.findByUsername("admin").orElse(null);
            if (admin == null) {
                log.warn("Admin user not found — skipping password check.");
                return;
            }

            String stored = admin.getPassword();
            boolean alreadyEncoded = stored != null && stored.startsWith("$2a$");

            if (!alreadyEncoded) {
                String rawPassword = (stored != null && !stored.isBlank()) ? stored : "admin";
                admin.setPassword(passwordEncoder.encode(rawPassword));
                employeeRepository.save(admin);
                log.info("Admin password was plain-text — re-encoded with BCrypt successfully.");
            } else {
                log.info("Admin password is already BCrypt-encoded. No action needed.");
                log.info("Admin hash in DB: {}", stored.substring(0, Math.min(20, stored.length())) + "...");
            }
        } catch (Exception e) {
            log.error("AdminPasswordInitializer failed: {}", e.getMessage(), e);
        }
    }
}
