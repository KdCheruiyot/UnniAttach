package com.dan.backend.repository;

import com.dan.backend.entity.Attachment;
import com.dan.backend.enums.AttachmentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findAllByOrderByCreatedAtDesc();

    List<Attachment> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Attachment> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<Attachment> findBySupervisorIdOrderByCreatedAtDesc(Long supervisorId);

    boolean existsByStudentIdAndStatus(Long studentId, AttachmentStatus status);

    long countByStatus(AttachmentStatus status);
}
