package com.ian.web.employee.otherinfo;

import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OtherInfoController {

    private final OtherInfoRepository otherInfoRepository;
}
