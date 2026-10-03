package com.jobmatcher.controller;

import com.jobmatcher.dto.JobResponse;
import com.jobmatcher.dto.PagedJobsResponse;
import com.jobmatcher.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // Public: homepage can browse all jobs without logging in.
    @GetMapping
    public ResponseEntity<PagedJobsResponse> getAllJobs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String domain,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        return ResponseEntity.ok(jobService.searchJobs(q, type, domain, page, size));
    }

    // Logged-in users: jobs in the domain detected from their resume.
    @GetMapping("/recommended")
    public ResponseEntity<PagedJobsResponse> getRecommendedJobs(
            Authentication authentication,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        return ResponseEntity.ok(
                jobService.getRecommendedJobs(authentication.getName(), q, type, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }
}