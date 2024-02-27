package com.ian.web.systemsettings.position_title;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import com.ian.web.systemsettings.levels.Level;
import com.ian.web.systemsettings.position_title.competencies.Competency;
import com.ian.web.systemsettings.salary_grades.SalaryGrade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PositionTitleModel {
    private Long id;
    @NotBlank(message = " is mandatory.")
    private String positionTitleName;
    @NotBlank(message = " is mandatory.")
    private String departmentCode;
    @NotNull(message = " is mandatory.")
    private Long employeeStatusId;
    @NotNull(message = " is mandatory.")
    private Long levelId;
    @NotNull(message = " is mandatory.")
    private Long salaryGradeId;
}
