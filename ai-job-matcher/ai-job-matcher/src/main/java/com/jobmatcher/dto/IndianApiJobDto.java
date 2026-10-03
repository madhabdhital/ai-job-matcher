package com.jobmatcher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IndianApiJobDto {

    private Long id;
    private String title;
    private String company;

    @JsonProperty("job_description")
    private String jobDescription;

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("job_type")
    private String jobType;

    private String location;

    @JsonProperty("apply_link")
    private String applyLink;

    @JsonProperty("posted_date")
    private String postedDate;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getJobType() {
        return jobType;
    }

    public String getLocation() {
        return location;
    }

    public String getApplyLink() {
        return applyLink;
    }

    public String getPostedDate() {
        return postedDate;
    }
}
