package com.dan.backend.controller;

import com.dan.backend.dto.Dtos.AssessmentRequest;
import com.dan.backend.dto.Dtos.AssessmentResponse;
import com.dan.backend.security.AuthUser;
import com.dan.backend.service.AssessmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/api/attachments/{attachmentId}/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY','SUPERVISOR')")
    public AssessmentResponse create(@PathVariable Long attachmentId, @AuthenticationPrincipal AuthUser me,
                                     @Valid @RequestBody AssessmentRequest request) {
        return assessmentService.create(attachmentId, me, request);
    }

    @GetMapping("/api/attachments/{attachmentId}/assessments")
    public List<AssessmentResponse> list(@PathVariable Long attachmentId, @AuthenticationPrincipal AuthUser me) {
        return assessmentService.list(attachmentId, me);
    }
}
