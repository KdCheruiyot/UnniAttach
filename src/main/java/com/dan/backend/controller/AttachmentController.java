package com.dan.backend.controller;

import com.dan.backend.dto.Dtos.AssignSupervisorRequest;
import com.dan.backend.dto.Dtos.AttachmentResponse;
import com.dan.backend.security.AuthUser;
import com.dan.backend.service.AttachmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /** Students, companies and supervisors see their own attachments; admins see all. */
    @GetMapping
    public List<AttachmentResponse> list(@AuthenticationPrincipal AuthUser me) {
        return attachmentService.list(me);
    }

    @GetMapping("/{id}")
    public AttachmentResponse get(@PathVariable Long id, @AuthenticationPrincipal AuthUser me) {
        return attachmentService.get(id, me);
    }

    @PatchMapping("/{id}/supervisor")
    @PreAuthorize("hasRole('ADMIN')")
    public AttachmentResponse assignSupervisor(@PathVariable Long id,
                                               @Valid @RequestBody AssignSupervisorRequest request) {
        return attachmentService.assignSupervisor(id, request);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('COMPANY','ADMIN')")
    public AttachmentResponse complete(@PathVariable Long id, @AuthenticationPrincipal AuthUser me) {
        return attachmentService.complete(id, me);
    }
}
