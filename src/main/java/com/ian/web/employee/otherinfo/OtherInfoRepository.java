package com.ian.web.employee.otherinfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtherInfoRepository extends JpaRepository<OtherInfo, Long> {

}
