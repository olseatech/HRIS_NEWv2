package com.ian.web.employee.clearance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClearanceApproversRepository extends JpaRepository<ClearanceApprovers, Long> {

}
