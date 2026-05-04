package com.ian.web.config.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.junit.jupiter.api.Test;

public class PasswordDebugTest {

    @Test
    public void testPasswordEncoding() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String rawPassword = "admin";
        String storedHash = "$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUm";

        // Test if the hash matches
        boolean matches = encoder.matches(rawPassword, storedHash);
        System.out.println("Does 'admin' match the hash? " + matches);

        if (!matches) {
            System.out.println("Hash does NOT match! Generating a new one...");
            String newHash = encoder.encode(rawPassword);
            System.out.println("New generated hash: " + newHash);
            System.out.println("Test with new hash: " + encoder.matches(rawPassword, newHash));
        }
    }
}
