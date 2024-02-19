package com.ian.web.systemsettings.position_title;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionTitleRepository extends JpaRepository<PositionTitle, Long> {
    
}
