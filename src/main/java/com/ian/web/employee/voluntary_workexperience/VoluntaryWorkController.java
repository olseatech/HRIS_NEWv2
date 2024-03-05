package com.ian.web.employee.voluntary_workexperience;

import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class VoluntaryWorkController {
    
    private final VoluntaryWorkRepository voluntaryWorkRepository;
}
