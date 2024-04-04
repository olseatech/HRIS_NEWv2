package com.ian.web.changepassword;

import javax.validation.constraints.NotBlank;

import com.ian.web.employee.Employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePassword {
    @NotBlank(message = " is mandatory.")
    private String oldPassword;
    @NotBlank(message = " is mandatory.")
    private String newPassword;
    @NotBlank(message = " is mandatory.")
    private String confirmPassword;
}
