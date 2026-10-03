package com.jobmatcher.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatcher.dto.IndianApiJobDto;
import com.jobmatcher.entity.JobListing;
import com.jobmatcher.repository.JobListingRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class IndianApiJobService {

    @Value("${indianapi.jobs.url}")
    private String jobsUrl;

    @Value("${indianapi.jobs.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final JobListingRepository jobListingRepository;

    public IndianApiJobService(
            ObjectMapper objectMapper,
            JobListingRepository jobListingRepository) {

        this.objectMapper = objectMapper;
        this.jobListingRepository = jobListingRepository;
    }

    public int fetchAndSaveJobs() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                jobsUrl,
                HttpMethod.GET,
                request,
                String.class
        );

        try {

            List<IndianApiJobDto> jobs =
                    objectMapper.readValue(
                            response.getBody(),
                            new TypeReference<List<IndianApiJobDto>>() {}
                    );

            int savedCount = 0;

            for (IndianApiJobDto apiJob : jobs) {

                // Avoid duplicate jobs
                if (jobListingRepository
                        .findByExternalJobId(apiJob.getId())
                        .isPresent()) {
                    continue;
                }

                JobListing job = new JobListing();

                job.setExternalJobId(apiJob.getId());
                job.setTitle(
                        apiJob.getJobTitle() != null
                                ? apiJob.getJobTitle()
                                : apiJob.getTitle()
                );
                job.setCompany(apiJob.getCompany());
                job.setLocation(apiJob.getLocation());
                job.setApplyUrl(apiJob.getApplyLink());
                job.setDescription(apiJob.getJobDescription());
                job.setSource("IndianAPI");
                job.setDomain(DomainMatcher.detect(job.getTitle(), job.getDescription()));

                // Convert API job type to our enum
                if (apiJob.getJobType() != null &&
                        apiJob.getJobType().toLowerCase().contains("intern")) {

                    job.setJobType(JobListing.JobType.INTERNSHIP);

                } else {

                    job.setJobType(JobListing.JobType.FULL_TIME);
                }

                jobListingRepository.save(job);
                savedCount++;
            }

            return savedCount;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to process jobs from IndianAPI",
                    e
            );
        }
    }
}