package com.dan.backend.repository;

import com.dan.backend.entity.Application;
import com.dan.backend.enums.ApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    List<Application> findByPlacementIdOrderByAppliedAtDesc(Long placementId);

    List<Application> findByStudentIdAndStatus(Long studentId, ApplicationStatus status);

    boolean existsByStudentIdAndPlacementId(Long studentId, Long placementId);

    long countByPlacementIdAndStatus(Long placementId, ApplicationStatus status);
}
