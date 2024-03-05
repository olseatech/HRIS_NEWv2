package com.ian.web.employee.govermentid;

import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class GovermentIssuedIdController {
    private final GovermentIssuedIdRepository govermentIssuedIdRepository;
}
