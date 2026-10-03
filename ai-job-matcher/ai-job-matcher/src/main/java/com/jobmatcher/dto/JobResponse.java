package com.jobmatcher.dto;

import com.jobmatcher.entity.JobListing.JobType;
import com.jobmatcher.entity.JobListing.WorkMode;
import java.time.LocalDateTime;

public class JobResponse {
    private Long id;
    private String title;
    private String company;
    private String location;
    private JobType jobType;
    private WorkMode workMode;
    private String domain;
    private String applyUrl;
    private String description;
    private String source;
    private LocalDateTime createdAt;

    public JobResponse(Long id, String title, String company, String location,
                       JobType jobType, WorkMode workMode, String domain,
                       String applyUrl, String description, String source,
                       LocalDateTime createdAt) {
        this.id=id; this.title=title; this.company=company; this.location=location;
        this.jobType=jobType; this.workMode=workMode; this.domain=domain;
        this.applyUrl=applyUrl; this.description=description; this.source=source; this.createdAt=createdAt;
    }

    public Long getId(){return id;} public String getTitle(){return title;}
    public String getCompany(){return company;} public String getLocation(){return location;}
    public JobType getJobType(){return jobType;} public WorkMode getWorkMode(){return workMode;}
    public String getDomain(){return domain;} public String getApplyUrl(){return applyUrl;}
    public String getDescription(){return description;} public String getSource(){return source;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
