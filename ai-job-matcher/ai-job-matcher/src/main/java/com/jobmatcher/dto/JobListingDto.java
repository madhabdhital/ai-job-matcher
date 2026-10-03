package com.jobmatcher.dto;

import com.jobmatcher.entity.JobListing.JobType;
import com.jobmatcher.entity.JobListing.WorkMode;
import jakarta.validation.constraints.NotBlank;

public class JobListingDto
{

    @NotBlank(message = "Job title is required")
    private String title;

    private String company;
    private String location;
    private JobType jobType;
    private WorkMode workMode;
    private String applyUrl;
    private String description;
    private String source;

    public JobListingDto() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public JobType getJobType() { return jobType; }
    public void setJobType(JobType jobType) { this.jobType = jobType; }
    public WorkMode getWorkMode() { return workMode; }
    public void setWorkMode(WorkMode workMode) { this.workMode = workMode; }
    public String getApplyUrl() { return applyUrl; }
    public void setApplyUrl(String applyUrl) { this.applyUrl = applyUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}