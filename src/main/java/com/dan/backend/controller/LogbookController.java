package com.dan.backend.controller;

import com.dan.backend.dto.Dtos.LogbookRequest;
import com.dan.backend.dto.Dtos.LogbookResponse;
import com.dan.backend.dto.Dtos.LogbookUpdateRequest;
import com.dan.backend.dto.Dtos.ReviewRequest;
import com.dan.backend.security.AuthUser;
import com.dan.backend.service.LogbookService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogbookController {

    private final LogbookService logbookService;

    public LogbookController(LogbookService logbookService) {
        this.logbookService = logbookService;
    }

    @PostMapping("/api/attachments/{attachmentId}/logbook")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public LogbookResponse add(@PathVariable Long attachmentId, @AuthenticationPrincipal AuthUser me,
                               @Valid @RequestBody LogbookRequest request) {
        return logbookService.add(attachmentId, me, request);
    }

    @GetMapping("/api/attachments/{attachmentId}/logbook")
    public List<LogbookResponse> list(@PathVariable Long attachmentId, @AuthenticationPrincipal AuthUser me) {
        return logbookService.list(attachmentId, me);
    }

    @PutMapping("/api/logbook/{entryId}")
    @PreAuthorize("hasRole('STUDENT')")
    public LogbookResponse update(@PathVariable Long entryId, @AuthenticationPrincipal AuthUser me,
                                  @Valid @RequestBody LogbookUpdateRequest request) {
        return logbookService.update(entryId, me, request);
    }

    @PatchMapping("/api/logbook/{entryId}/review")
    @PreAuthorize("hasAnyRole('COMPANY','SUPERVISOR')")
    public LogbookResponse review(@PathVariable Long entryId, @AuthenticationPrincipal AuthUser me,
                                  @Valid @RequestBody ReviewRequest request) {
        return logbookService.review(entryId, me, request);
    }
}
