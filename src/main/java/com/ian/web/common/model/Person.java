package com.ian.web.common.model;

import java.time.LocalDate;
import java.time.Period;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ian.web.config.data.Auditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
@AllArgsConstructor
@MappedSuperclass
@Data
public class Person extends Auditable {
	
	@NotBlank(message = " is mandatory.")
    private String firstName;
	
	@NotBlank(message = " is mandatory.")
    private String lastName;
	
	private String middleName;
	
	private String prefix;
	private String suffix;
	private String birthPlace;
	
	
	@NotBlank(message = " is mandatory.")
    private String gender;
		
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthdate;
	
	
    
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
    
    @JsonIgnore
	public Integer getAge() {
		return Period.between(getBirthdate(), LocalDate.now()).getYears();
	}

}
