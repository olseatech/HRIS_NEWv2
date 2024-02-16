package com.ian.web.systemsettings.position_title;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.ian.web.constants.EmploymentType;

import net.sf.jasperreports.engine.json.expression.member.MemberExpression.DIRECTION;

public class PositionTitle {
    private Long id;
    private String positionTitleName;
    private String departmentCode;
    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;
}
