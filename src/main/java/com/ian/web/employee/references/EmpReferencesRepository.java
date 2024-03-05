package com.ian.web.employee.references;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpReferencesRepository extends JpaRepository<EmpReferences, Long> {
    
}
