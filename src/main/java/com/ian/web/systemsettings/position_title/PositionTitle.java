package com.ian.web.systemsettings.position_title;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import com.ian.web.systemsettings.salary_grades.SalaryGrade;

import ch.qos.logback.classic.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "position_title")
@Entity
@Builder
public class PositionTitle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String positionTitleName;
    private String departmentCode;
    private EmployeeStatus employeeStatus;
    private Level level;
    private SalaryGrade salaryGrade;
    private String education;
    private String training;
    private String eligibility;
    private List<String> competencies;
}
