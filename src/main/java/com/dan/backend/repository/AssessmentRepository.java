package com.dan.backend.repository;

import com.dan.backend.entity.Assessment;
import com.dan.backend.enums.AssessmentType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByAttachmentIdOrderByCreatedAtAsc(Long attachmentId);

    boolean existsByAttachmentIdAndType(Long attachmentId, AssessmentType type);
}
