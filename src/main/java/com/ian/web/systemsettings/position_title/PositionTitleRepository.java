package com.ian.web.systemsettings.position_title;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface PositionTitleRepository extends JpaRepository<PositionTitle, Long> {
}
