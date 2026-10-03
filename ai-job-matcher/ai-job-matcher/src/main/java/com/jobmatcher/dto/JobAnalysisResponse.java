
package com.jobmatcher.dto;

import java.time.LocalDateTime;

public class JobAnalysisResponse {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;

    private Integer matchPercentage;
    private String matchedSkills;
    private String missingSkills;
    private String tailoredResume;
    private String coverLetter;
    private String outreachMsg;
    private LocalDateTime analyzedAt;

    public JobAnalysisResponse() {
    }

    public JobAnalysisResponse(
            Long id,
            Long jobId,
            String jobTitle,
            String company,
            Integer matchPercentage,
            String matchedSkills,
            String missingSkills,
            String tailoredResume,
            String coverLetter,
            String outreachMsg,
            LocalDateTime analyzedAt) {

        this.id = id;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.company = company;
        this.matchPercentage = matchPercentage;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.tailoredResume = tailoredResume;
        this.coverLetter = coverLetter;
        this.outreachMsg = outreachMsg;
        this.analyzedAt = analyzedAt;
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

    public Integer getMatchPercentage() {
        return matchPercentage;
    }

    public String getMatchedSkills() {
        return matchedSkills;
    }

    public String getMissingSkills() {
        return missingSkills;
    }

    public String getTailoredResume() {
        return tailoredResume;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public String getOutreachMsg() {
        return outreachMsg;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }
}

