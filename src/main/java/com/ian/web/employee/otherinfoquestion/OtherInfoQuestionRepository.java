package com.ian.web.employee.otherinfoquestion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtherInfoQuestionRepository extends JpaRepository<OtherInfoQuestion, Long>{
    List<OtherInfoQuestion> findByEmployeeId(Long id);
}
