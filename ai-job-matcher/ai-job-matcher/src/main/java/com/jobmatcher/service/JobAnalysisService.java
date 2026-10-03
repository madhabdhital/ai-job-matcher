package com.jobmatcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatcher.entity.JobAnalysis;
import com.jobmatcher.entity.JobListing;
import com.jobmatcher.entity.MasterResume;
import com.jobmatcher.entity.User;
import com.jobmatcher.repository.JobAnalysisRepository;
import com.jobmatcher.repository.JobListingRepository;
import com.jobmatcher.repository.MasterResumeRepository;
import com.jobmatcher.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class JobAnalysisService {

    private final JobAnalysisRepository jobAnalysisRepository;
    private final JobListingRepository jobListingRepository;
    private final MasterResumeRepository masterResumeRepository;
    private final UserRepository userRepository;
    private final LlmService llmService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JobAnalysisService(
            JobAnalysisRepository jobAnalysisRepository,
            JobListingRepository jobListingRepository,
            MasterResumeRepository masterResumeRepository,
            UserRepository userRepository,
            LlmService llmService) {

        this.jobAnalysisRepository = jobAnalysisRepository;
        this.jobListingRepository = jobListingRepository;
        this.masterResumeRepository = masterResumeRepository;
        this.userRepository = userRepository;
        this.llmService = llmService;
    }

    public JobAnalysis analyzeJob(
            Long jobId,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        MasterResume resume =
                masterResumeRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException(
                                "Please upload a master resume first."
                        ));

        String result = llmService.analyzeAndTailor(
                resume.getExtractedText(),
                job.getDescription()
        );

        try {
            JsonNode json = objectMapper.readTree(cleanJson(result));

            JobAnalysis analysis =
                    jobAnalysisRepository
                            .findByUserIdAndJobListingId(
                                    user.getId(),
                                    jobId
                            )
                            .orElse(new JobAnalysis());

            analysis.setUser(user);
            analysis.setJobListing(job);

            if (json.has("matchPercentage")) {
                analysis.setMatchPercentage(
                        json.get("matchPercentage").asInt()
                );
            }

            if (json.has("matchedSkills")) {
                analysis.setMatchedSkills(
                        objectMapper.writeValueAsString(
                                json.get("matchedSkills")
                        )
                );
            }

            if (json.has("missingSkills")) {
                analysis.setMissingSkills(
                        objectMapper.writeValueAsString(
                                json.get("missingSkills")
                        )
                );
            }

            if (json.has("tailoredResume")) {
                analysis.setTailoredResume(
                        json.get("tailoredResume").asText()
                );
            }

            if (json.has("coverLetter")) {
                analysis.setCoverLetter(
                        json.get("coverLetter").asText()
                );
            }

            return jobAnalysisRepository.save(analysis);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to process AI analysis response: "
                            + e.getMessage(),
                    e
            );
        }
    }

    public JobAnalysis getAnalysis(
            Long jobId,
            String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return jobAnalysisRepository
                .findByUserIdAndJobListingId(
                        user.getId(),
                        jobId
                )
                .orElseThrow(() -> new RuntimeException(
                        "No analysis found for this job"
                ));
    }

    private String cleanJson(String response) {

        if (response == null) {
            throw new RuntimeException("Empty AI response");
        }

        response = response.trim();

        // Gemini may return ```json ... ```
        if (response.startsWith("```")) {

            int firstNewLine = response.indexOf("\n");

            if (firstNewLine != -1) {
                response = response.substring(firstNewLine + 1);
            }

            if (response.endsWith("```")) {
                response = response.substring(
                        0,
                        response.length() - 3
                );
            }
        }

        return response.trim();
    }
}