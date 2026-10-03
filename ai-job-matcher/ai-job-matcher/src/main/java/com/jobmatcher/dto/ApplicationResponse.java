package com.jobmatcher.dto;

import java.time.LocalDateTime;

public class ApplicationResponse {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String location;
    private String applyUrl;
    private String status;
    private String notes;
    private LocalDateTime appliedDate;

    public ApplicationResponse(
            Long id,
            Long jobId,
            String jobTitle,
            String company,
            String location,
            String applyUrl,
            String status,
            String notes,
            LocalDateTime appliedDate) {

        this.id = id;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.company = company;
        this.location = location;
        this.applyUrl = applyUrl;
        this.status = status;
        this.notes = notes;
        this.appliedDate = appliedDate;
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }

    public String getApplyUrl() {
        return applyUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getAppliedDate() {
        return appliedDate;
    }
}
