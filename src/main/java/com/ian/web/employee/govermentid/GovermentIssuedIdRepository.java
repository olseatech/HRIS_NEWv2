package com.ian.web.employee.govermentid;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GovermentIssuedIdRepository extends JpaRepository<GovermentIssuedId, Long>{
    List<GovermentIssuedId> findByEmployeeId(Long id);
}
