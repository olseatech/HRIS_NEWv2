package com.ian.web.employee.approvers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRecordSignatoryRepository extends JpaRepository<ServiceRecordSignatory, Long> {

}
