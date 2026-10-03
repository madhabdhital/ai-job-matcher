package com.jobmatcher.service;

import com.jobmatcher.dto.JobResponse;
import com.jobmatcher.dto.PagedJobsResponse;
import com.jobmatcher.entity.JobListing;
import com.jobmatcher.entity.MasterResume;
import com.jobmatcher.entity.User;
import com.jobmatcher.repository.JobListingRepository;
import com.jobmatcher.repository.MasterResumeRepository;
import com.jobmatcher.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class JobService {

    private static final int MAX_PAGE_SIZE = 50;

    private final JobListingRepository jobListingRepository;
    private final MasterResumeRepository masterResumeRepository;
    private final UserRepository userRepository;

    public JobService(JobListingRepository jobListingRepository,
                      MasterResumeRepository masterResumeRepository,
                      UserRepository userRepository) {
        this.jobListingRepository = jobListingRepository;
        this.masterResumeRepository = masterResumeRepository;
        this.userRepository = userRepository;
    }

    // Older jobs may have been saved without a domain: fill it in once at startup.
    @EventListener(ApplicationReadyEvent.class)
    public void backfillMissingDomains() {
        Specification<JobListing> missingDomain = (root, query, cb) ->
                cb.or(cb.isNull(root.get("domain")), cb.equal(root.get("domain"), ""));

        List<JobListing> missing = jobListingRepository.findAll(missingDomain);

        for (JobListing job : missing) {
            job.setDomain(DomainMatcher.detect(job.getTitle(), job.getDescription()));
        }

        if (!missing.isEmpty()) {
            jobListingRepository.saveAll(missing);
        }
    }

    public PagedJobsResponse searchJobs(String q, String type, String domain, int page, int size) {
        Page<JobListing> result =
                jobListingRepository.findAll(filter(q, type, domain), pageable(page, size));

        List<JobResponse> jobs = result.getContent().stream()
                .map(job -> toResponse(job, false))
                .toList();

        return new PagedJobsResponse(jobs, result.getNumber(), result.getTotalPages(),
                result.getTotalElements());
    }

    public PagedJobsResponse getRecommendedJobs(String userEmail, String q, String type,
                                                int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MasterResume resume = masterResumeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Please upload your resume first."));

        String domain = resume.getSpecialization();
        if (domain != null) domain = domain.replace("\"", "").trim();
        if (domain == null || domain.isBlank() || "General".equalsIgnoreCase(domain)) {
            domain = null; // no specific domain detected: show everything
        }

        return searchJobs(q, type, domain, page, size);
    }

    public JobResponse getJobById(Long jobId) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return toResponse(job, true);
    }

    private Specification<JobListing> filter(String q, String type, String domain) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("company")), like)));
            }

            if (type != null && !type.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("jobType"),
                            JobListing.JobType.valueOf(type.trim().toUpperCase(Locale.ROOT))));
                } catch (IllegalArgumentException ignored) {
                    // unknown job type: ignore the filter
                }
            }

            if (domain != null && !domain.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("domain")),
                        domain.trim().toLowerCase(Locale.ROOT)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable pageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        return PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }

    private JobResponse toResponse(JobListing job, boolean includeDescription) {
        String domain = job.getDomain();
        if (domain == null || domain.isBlank()) {
            domain = DomainMatcher.detect(job.getTitle(), job.getDescription());
        }

        return new JobResponse(job.getId(), job.getTitle(), job.getCompany(),
                job.getLocation(), job.getJobType(), job.getWorkMode(), domain,
                job.getApplyUrl(), includeDescription ? job.getDescription() : null,
                job.getSource(), job.getCreatedAt());
    }
}