package com.ian.web.systemsettings.degreelevels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeLevelRepository extends JpaRepository<DegreeLevel, Long>{
    
}
