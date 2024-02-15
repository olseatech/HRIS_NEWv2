package com.ian.web.systemsettings.degree_courses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeCoursesRepository extends JpaRepository<DegreeCourses, Long> {
    
}
