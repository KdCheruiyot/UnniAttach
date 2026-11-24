package com.dan.backend.repository;

import com.dan.backend.entity.LogbookEntry;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogbookEntryRepository extends JpaRepository<LogbookEntry, Long> {

    List<LogbookEntry> findByAttachmentIdOrderByEntryDateDesc(Long attachmentId);

    boolean existsByAttachmentIdAndEntryDate(Long attachmentId, LocalDate entryDate);
}
