package com.dan.backend.controller;

import com.dan.backend.dto.Dtos.ApplicationRequest;
import com.dan.backend.dto.Dtos.ApplicationResponse;
import com.dan.backend.dto.Dtos.DecisionRequest;
import com.dan.backend.security.AuthUser;
import com.dan.backend.service.ApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/api/applications")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public ApplicationResponse apply(@AuthenticationPrincipal AuthUser me,
                                     @Valid @RequestBody ApplicationRequest request) {
        return applicationService.apply(me, request);
    }

    @GetMapping("/api/applications/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ApplicationResponse> mine(@AuthenticationPrincipal AuthUser me) {
        return applicationService.mine(me);
    }

    @PatchMapping("/api/applications/{id}/withdraw")
    @PreAuthorize("hasRole('STUDENT')")
    public ApplicationResponse withdraw(@PathVariable Long id, @AuthenticationPrincipal AuthUser me) {
        return applicationService.withdraw(id, me);
    }

    @PatchMapping("/api/applications/{id}/decision")
    @PreAuthorize("hasRole('COMPANY')")
    public ApplicationResponse decide(@PathVariable Long id, @AuthenticationPrincipal AuthUser me,
                                      @Valid @RequestBody DecisionRequest request) {
        return applicationService.decide(id, me, request);
    }

    @GetMapping("/api/placements/{placementId}/applications")
    @PreAuthorize("hasAnyRole('COMPANY','ADMIN')")
    public List<ApplicationResponse> forPlacement(@PathVariable Long placementId,
                                                  @AuthenticationPrincipal AuthUser me) {
        return applicationService.forPlacement(placementId, me);
    }
}
