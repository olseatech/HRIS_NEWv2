package com.ian.web.nav;

import javax.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.systemsettings.academichonors.AcademicHonorsRepository;
import com.ian.web.systemsettings.degree_courses.DegreeCoursesRepository;
import com.ian.web.systemsettings.degreelevels.DegreeLevelRepository;
import com.ian.web.systemsettings.scholarship.ScholarshipRepository;
import com.ian.web.systemsettings.schools.SchoolRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NavController {
	
	private final FamilyBgRepository familyBgRepository;
	private final EducationalBackgroundRepository educationalBackgroundRepository;
	
	@GetMapping("/dashboard")
    public String dashboard(HttpSession session, Authentication auth) {
		Employee actor = (Employee) auth.getPrincipal();
		actor.setFamilyBgCount(1);
		actor.setEducationalBgCount(1);
//		actor.setVoluntaryWorkCount(0);
        session.setAttribute("actorObj", actor);
                
        return "dashboard";
    }

}
