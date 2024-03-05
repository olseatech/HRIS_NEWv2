package com.ian.web.employee.references;

import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EmpReferencesController {

    private final EmpReferencesRepository empReferencesRepository;
}
