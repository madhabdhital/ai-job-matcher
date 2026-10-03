package com.jobmatcher.dto;

import java.util.List;

public class ResumeAnalysisResponse {

    private Integer matchPercentage;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String tailoredResume;
    private String coverLetter;

    public ResumeAnalysisResponse() {}

    public ResumeAnalysisResponse(Integer matchPercentage, List<String> matchedSkills, 
                                  List<String> missingSkills, String tailoredResume, String coverLetter) {
        this.matchPercentage = matchPercentage;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.tailoredResume = tailoredResume;
        this.coverLetter = coverLetter;
    }

    public Integer getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(Integer matchPercentage) { this.matchPercentage = matchPercentage; }
    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }
    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }
    public String getTailoredResume() { return tailoredResume; }
    public void setTailoredResume(String tailoredResume) { this.tailoredResume = tailoredResume; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
}