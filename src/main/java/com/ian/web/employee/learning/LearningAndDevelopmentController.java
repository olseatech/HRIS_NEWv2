package com.ian.web.employee.learning;

import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LearningAndDevelopmentController {
    
    private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
}
