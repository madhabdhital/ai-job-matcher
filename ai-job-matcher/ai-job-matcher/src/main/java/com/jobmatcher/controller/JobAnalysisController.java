
package com.jobmatcher.controller;

import com.jobmatcher.dto.JobAnalysisResponse;
import com.jobmatcher.entity.JobAnalysis;
import com.jobmatcher.service.JobAnalysisService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/job-analysis")
public class JobAnalysisController {

    private final JobAnalysisService jobAnalysisService;

    public JobAnalysisController(
            JobAnalysisService jobAnalysisService) {

        this.jobAnalysisService = jobAnalysisService;
    }

    @PostMapping("/{jobId}")
    public ResponseEntity<JobAnalysisResponse> analyzeJob(
            @PathVariable Long jobId,
            Authentication authentication) {

        JobAnalysis analysis =
                jobAnalysisService.analyzeJob(
                        jobId,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                toResponse(analysis)
        );
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobAnalysisResponse> getAnalysis(
            @PathVariable Long jobId,
            Authentication authentication) {

        JobAnalysis analysis =
                jobAnalysisService.getAnalysis(
                        jobId,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                toResponse(analysis)
        );
    }

    private JobAnalysisResponse toResponse(
            JobAnalysis analysis) {

        return new JobAnalysisResponse(
                analysis.getId(),
                analysis.getJobListing().getId(),
                analysis.getJobListing().getTitle(),
                analysis.getJobListing().getCompany(),
                analysis.getMatchPercentage(),
                analysis.getMatchedSkills(),
                analysis.getMissingSkills(),
                analysis.getTailoredResume(),
                analysis.getCoverLetter(),
                analysis.getOutreachMsg(),
                analysis.getAnalyzedAt()
        );
    }
}

