package com.ian.web.employee;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

import com.ian.web.common.model.Person;
import com.ian.web.employee.familybg.FamilyBg;
import com.ian.web.systemsettings.division.Division;
import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import com.ian.web.systemsettings.position_title.PositionTitle;

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
	
	@Transient
	private String showMode;
	
	private String empHashCode;
	private String empNo;
	private String username;
	private String password;
	
	@Transient
	private String confirmPassword;

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
    private String countryOfOrigin;
    private String birthPlace;
    private String telNo;
    
    @Email(message = "Invalid email.")
    private String email1;	
    @Email(message = "Invalid email.")
    private String email2;
    
    private String mobileNo1;
    private String mobileNo2;
	
    private String status = "ACTIVE";
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "division_id")
    private Division division;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_status_id")
    private EmployeeStatus employeeStatus;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_title_id")
    private PositionTitle positionTitle;
    
    @NotBlank
    private String userType;
    
    private String profilePhoto;
    
    
    private String houseno1;
    private String houseno2;
    private String street1;
    private String street2;
    private String subdivision1;
    private String subdivision2;
    private String brgy1;
    private String brgy2;
    private String city1;
    private String city2;
    private String province1;
    private String province2;
    private String zipcode1;
    private String zipcode2;
    
    
	
	@Transient
	private MultipartFile photoFile;
	
	@Transient
	private List<FamilyBg> familyBgList;
	
	@Transient
	private PdsCountDto pdsCountDto;

//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "employee_educational_background", referencedColumnName = "id")
//    private List<EducationalBackground> educationalBackgrounds;
//
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "employee_work_experience", referencedColumnName = "id")
//    private List<WorkExperience> workExperiences;
//
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "employee_voluntary_experience", referencedColumnName = "id")
//    private List<VoluntaryWork> voluntaryWorks;
//
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "employee_other_info", referencedColumnName = "id")
//    private List<OtherInfo> otherInfos;
//
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "employee_references", referencedColumnName = "id")
//    private List<EmpReferences> empReferences;
//
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "employee_eligibility", referencedColumnName = "id")
//    private List<CivilServiceEligibility> civilServiceEligibilities;
//
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "employee_learning_development", referencedColumnName = "id")
//    private List<LearningAndDevelopment> learningAndDevelopments;
//
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "employee_government_id", referencedColumnName = "id")
//    private List<GovermentIssuedId> govermentIssuedIds;
    
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
