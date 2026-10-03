package com.jobmatcher.service;

import com.jobmatcher.dto.ApplicationRequest;
import com.jobmatcher.dto.ApplicationResponse;
import com.jobmatcher.entity.ApplicationTracker;
import com.jobmatcher.entity.JobListing;
import com.jobmatcher.entity.User;
import com.jobmatcher.repository.ApplicationTrackerRepository;
import com.jobmatcher.repository.JobListingRepository;
import com.jobmatcher.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationTrackerRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobListingRepository jobRepository;

    public ApplicationService(
            ApplicationTrackerRepository applicationRepository,
            UserRepository userRepository,
            JobListingRepository jobRepository) {

        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    // Create application

                public ApplicationTracker createApplication(
                        ApplicationRequest request,
                        String email) {

                User user = userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException("User not found"));

                JobListing job = jobRepository.findById(request.getJobId())
                        .orElseThrow(() ->
                                new RuntimeException("Job not found"));

                // Prevent duplicate application tracking
               boolean alreadyTracked =
                applicationRepository.existsByUserIdAndJobId(
                        user.getId(),
                        job.getId()
                );

                if (alreadyTracked) {
                        throw new RuntimeException(
                                "You have already tracked this application."
                        );
                }

                ApplicationTracker application =
                        new ApplicationTracker();

                application.setUser(user);
                application.setJob(job);
                application.setStatus(
                        ApplicationTracker.Status.APPLIED
                );
                application.setNotes(request.getNotes());

                return applicationRepository.save(application);
                }



    // Get all applications of logged-in user
    public List<ApplicationResponse> getUserApplications(
            String userEmail) {

        User user = getUserByEmail(userEmail);

        return applicationRepository.findByUserId(user.getId())
                .stream()
                .map(application -> {

                    JobListing job = application.getJob();

                    return new ApplicationResponse(
                            application.getId(),
                            job.getId(),
                            job.getTitle(),
                            job.getCompany(),
                            job.getLocation(),
                            job.getApplyUrl(),
                            application.getStatus().name(),
                            application.getNotes(),
                            application.getAppliedDate()
                    );
                })
                .toList();
    }

    // Update application status
    public ApplicationTracker updateStatus(
            Long applicationId,
            ApplicationTracker.Status status,
            String userEmail) {

        User user = getUserByEmail(userEmail);

        ApplicationTracker application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() -> new RuntimeException(
                                "Application not found"
                        ));

        if (application.getUser() == null ||
                !application.getUser().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "Unauthorized to update this application"
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Application status cannot be null"
            );
        }

        application.setStatus(status);

        return applicationRepository.save(application);
    }

    // Delete application
    public void deleteApplication(
            Long applicationId,
            String userEmail) {

        User user = getUserByEmail(userEmail);

        ApplicationTracker application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() -> new RuntimeException(
                                "Application not found"
                        ));

        if (application.getUser() == null ||
                !application.getUser().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "Unauthorized to delete this application"
            );
        }

        applicationRepository.delete(application);
    }

    private User getUserByEmail(String userEmail) {

        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"
                ));
    }
}