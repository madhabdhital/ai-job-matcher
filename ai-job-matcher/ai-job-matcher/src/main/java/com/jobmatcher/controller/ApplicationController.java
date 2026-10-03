package com.jobmatcher.controller;

import com.jobmatcher.dto.ApplicationRequest;
import com.jobmatcher.dto.ApplicationResponse;
import com.jobmatcher.entity.ApplicationTracker;
import com.jobmatcher.service.ApplicationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // Create application
    @PostMapping
    public ResponseEntity<ApplicationTracker> createApplication(
            @Valid @RequestBody ApplicationRequest request,
            Authentication authentication) {

        ApplicationTracker application =
                applicationService.createApplication(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(application);
    }

    // Get all applications
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getApplications(
            Authentication authentication) {

        return ResponseEntity.ok(
                applicationService.getUserApplications(
                        authentication.getName()
                )
        );
    }

    // Update application status
    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationTracker> updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationTracker.Status status,
            Authentication authentication) {

        return ResponseEntity.ok(
                applicationService.updateStatus(
                        id,
                        status,
                        authentication.getName()
                )
        );
    }

    // Delete application
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteApplication(
            @PathVariable Long id,
            Authentication authentication) {

        applicationService.deleteApplication(
                id,
                authentication.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Application deleted successfully"
                )
        );
    }
}