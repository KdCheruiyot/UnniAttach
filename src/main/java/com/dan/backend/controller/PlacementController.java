package com.dan.backend.controller;

import com.dan.backend.dto.Dtos.PlacementRequest;
import com.dan.backend.dto.Dtos.PlacementResponse;
import com.dan.backend.security.AuthUser;
import com.dan.backend.service.PlacementService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/placements")
public class PlacementController {

    private final PlacementService placementService;

    public PlacementController(PlacementService placementService) {
        this.placementService = placementService;
    }

    /** Open placements. Optional ?q= searches title, location and required skills. */
    @GetMapping
    public List<PlacementResponse> listOpen(@RequestParam(required = false) String q) {
        return placementService.listOpen(q);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('COMPANY')")
    public List<PlacementResponse> mine(@AuthenticationPrincipal AuthUser me) {
        return placementService.mine(me);
    }

    @GetMapping("/{id}")
    public PlacementResponse get(@PathVariable Long id) {
        return placementService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COMPANY')")
    public PlacementResponse create(@AuthenticationPrincipal AuthUser me,
                                    @Valid @RequestBody PlacementRequest request) {
        return placementService.create(me, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY','ADMIN')")
    public PlacementResponse update(@PathVariable Long id, @AuthenticationPrincipal AuthUser me,
                                    @Valid @RequestBody PlacementRequest request) {
        return placementService.update(id, me, request);
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('COMPANY','ADMIN')")
    public PlacementResponse close(@PathVariable Long id, @AuthenticationPrincipal AuthUser me) {
        return placementService.close(id, me);
    }
}
