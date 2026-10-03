package com.jobmatcher.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_analyses")
public class JobAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobListing jobListing;

    private Integer matchPercentage;

    @Column(columnDefinition = "JSON")
    private String matchedSkills;

    @Column(columnDefinition = "JSON")
    private String missingSkills;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String tailoredResume;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @Column(columnDefinition = "TEXT")
    private String outreachMsg;

    @Column(nullable = false, updatable = false)
    private LocalDateTime analyzedAt = LocalDateTime.now();

    public JobAnalysis() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public JobListing getJobListing() { return jobListing; }
    public void setJobListing(JobListing jobListing) { this.jobListing = jobListing; }
    public Integer getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(Integer matchPercentage) { this.matchPercentage = matchPercentage; }
    public String getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(String matchedSkills) { this.matchedSkills = matchedSkills; }
    public String getMissingSkills() { return missingSkills; }
    public void setMissingSkills(String missingSkills) { this.missingSkills = missingSkills; }
    public String getTailoredResume() { return tailoredResume; }
    public void setTailoredResume(String tailoredResume) { this.tailoredResume = tailoredResume; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    public String getOutreachMsg() { return outreachMsg; }
    public void setOutreachMsg(String outreachMsg) { this.outreachMsg = outreachMsg; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
}