package com.ian.web.employee;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import com.ian.web.common.model.Address;
import com.ian.web.common.model.Person;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "unique_username_employee", columnNames = "username")
})
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Employee extends Person  implements UserDetails {
	
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	private String empHashCode;
	private String empNo;
	private String username;
	private String password;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate assumptiondate;	
    
    private String plantillaNo;
    private String titleSuffix;
    private String civilStatus;
    private String height;
    private String weight;
    private String religion;
    private String bloodType;
    private String gsisBpNo;	
    private String gsisPolicyNo;
    private String gsisIdNo;
    private String pagibigNo;
    private String philhealthNo;
    private String sssNo;
    private String tin;
    private String citizenship;
    
    @Email(message = "Invalid email.")
    private String email1;	
    @Email(message = "Invalid email.")
    private String email2;
    
    private String mobileno1;
    private String mobileno2;
	
    private String status = "ACTIVE";
    
    @NotBlank
    private String userType;
    
    private String profilePhoto;
		
	private boolean isPermanentSame;
		
	@Transient
	private MultipartFile photoFile;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(this.userType));
        return authorities;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
	
}
