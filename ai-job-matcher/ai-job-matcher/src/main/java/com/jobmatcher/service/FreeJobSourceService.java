package com.jobmatcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatcher.entity.JobListing;
import com.jobmatcher.repository.JobListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

@Service
public class FreeJobSourceService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JobListingRepository repository;

    public FreeJobSourceService(JobListingRepository repository) {
        this.repository = repository;
    }

    /**
     * Himalayas exposes a free, no-auth public JSON API.
     * We sync a small page and an internship search so the application
     * has both general remote jobs and internship opportunities.
     */
    public int syncHimalayas() {
        int saved = 0;
        saved += sync("https://himalayas.app/jobs/api?limit=20", false);
        saved += sync("https://himalayas.app/jobs/api/search?q=internship&page=1", true);
        return saved;
    }

    private int sync(String url, boolean internshipSearch) {
        try {
            JsonNode root = objectMapper.readTree(
                    restTemplate.getForObject(url, String.class)
            );
            JsonNode jobs = root.path("jobs");
            int saved = 0;

            if (!jobs.isArray()) return 0;

            for (JsonNode item : jobs) {
                String guid = item.path("guid").asText(
                        item.path("applicationLink").asText("")
                );
                if (guid.isBlank()) continue;

                long externalId = -Integer.toUnsignedLong(guid.hashCode());

                // The existing schema identifies imported records by external ID.
                if (repository.findByExternalJobId(externalId).isPresent()) continue;

                String title = text(item, "title");
                String description = text(item, "description");
                String company = text(item, "companyName");
                String location = text(item, "location");
                String employmentType = text(item, "employmentType");

                JobListing job = new JobListing();
                job.setExternalJobId(externalId);
                job.setTitle(title.isBlank() ? "Untitled Opportunity" : title);
                job.setCompany(company);
                job.setLocation(location.isBlank() ? "Remote" : location);
                job.setDescription(description);
                job.setApplyUrl(guid);
                job.setSource("Himalayas");
                job.setDomain(DomainMatcher.detect(title, description));

                String type = (employmentType + " " + title).toLowerCase(Locale.ROOT);
                if (internshipSearch || type.contains("intern")) {
                    job.setJobType(JobListing.JobType.INTERNSHIP);
                } else if (type.contains("contract") || type.contains("freelance")) {
                    job.setJobType(JobListing.JobType.CONTRACT);
                } else {
                    job.setJobType(JobListing.JobType.FULL_TIME);
                }

                job.setWorkMode(JobListing.WorkMode.REMOTE);
                repository.save(job);
                saved++;
            }
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync Himalayas jobs", e);
        }
    }

    private String text(JsonNode node, String field) {
        return node.path(field).asText("");
    }
}
