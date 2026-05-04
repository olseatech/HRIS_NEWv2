package com.ian.web.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 5.7+ / Spring Boot 2.7+ style — no WebSecurityConfigurerAdapter.
 * Uses SecurityFilterChain beans instead of override methods.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Public routes — no authentication required. */
    private static final String[] PUBLIC_ROUTES = {
        "/login", "/logout", "/error", "/reports/**"
    };

    /** Static assets — bypassed entirely by the security filter chain. */
    private static final String[] STATIC_ASSETS = {
        "/assets/**", "/global_assets/**",
        "/admin_js/**", "/parent_css/**", "/parent_js/**",
        "/js/**", "/css/**", "/images/**"
    };

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // ------------------------------------------------------------------
    // Authentication provider — wires UserDetailsService + PasswordEncoder
    // ------------------------------------------------------------------
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ------------------------------------------------------------------
    // Main filter chain
    // ------------------------------------------------------------------
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            // CSRF: enabled globally; disabled only for REST API endpoints
            .csrf(csrf -> csrf.ignoringAntMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                .antMatchers(PUBLIC_ROUTES).permitAll()
                .antMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .sessionManagement(session -> session
                // Prevent session-fixation attacks
                .sessionFixation().migrateSession()
                .invalidSessionUrl("/login?expired")
            )
            .headers(headers -> headers
                // Allow same-origin iframes (needed for some report views)
                .frameOptions().sameOrigin()
            );

        return http.build();
    }

    // ------------------------------------------------------------------
    // Static-asset bypass — security filter skips these paths entirely
    // ------------------------------------------------------------------
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().antMatchers(STATIC_ASSETS);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
