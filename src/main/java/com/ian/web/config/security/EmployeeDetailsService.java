package com.ian.web.config.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.ian.web.employee.EmployeeRepository;

@Service
public class EmployeeDetailsService implements UserDetailsService {

    private EmployeeRepository employeeRepository;

    public EmployeeDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username != null) {
            // Use findByUsernameFetched (JOIN FETCH all four EAGER associations) so the
            // Employee stored in the Spring Security context and session.actorObj never
            // holds uninitialized Hibernate proxies.  Without this, any page that accesses
            // employee.division, positionTitle, district, or employeeStatus on the detached
            // session entity throws LazyInitializationException mid-Thymeleaf render, which
            // causes ERR_INCOMPLETE_CHUNKED_ENCODING because the response head is already
            // committed before the exception is caught.
            return this.employeeRepository.findByUsernameFetched(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Username not found."));
        }
        throw new UsernameNotFoundException("Username not found.");
    }
}
