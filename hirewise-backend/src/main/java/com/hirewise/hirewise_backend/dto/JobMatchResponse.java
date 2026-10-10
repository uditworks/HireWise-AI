package com.hirewise.hirewise_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class JobMatchResponse {

    private Long resumeId;
    private Long jobId;
    private String jobTitle;
    private int overallMatchScore;
    private int skillMatchScore;
    private int experienceMatchScore;
    private String fitCategory;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String explanation;
    private String disclaimer = "Fit scores are algorithmic estimates based on extracted keywords and do not guarantee hiring outcomes.";
    private LocalDateTime matchedAt;

    public JobMatchResponse() {
        this.matchedAt = LocalDateTime.now();
    }

    public JobMatchResponse(Long resumeId, Long jobId, String jobTitle,
                            int overallMatchScore, int skillMatchScore, int experienceMatchScore,
                            String fitCategory, List<String> matchedSkills, List<String> missingSkills,
                            String explanation) {
        this.resumeId = resumeId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.overallMatchScore = overallMatchScore;
        this.skillMatchScore = skillMatchScore;
        this.experienceMatchScore = experienceMatchScore;
        this.fitCategory = fitCategory;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.explanation = explanation;
        this.matchedAt = LocalDateTime.now();
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public int getOverallMatchScore() {
        return overallMatchScore;
    }

    public void setOverallMatchScore(int overallMatchScore) {
        this.overallMatchScore = overallMatchScore;
    }

    public int getSkillMatchScore() {
        return skillMatchScore;
    }

    public void setSkillMatchScore(int skillMatchScore) {
        this.skillMatchScore = skillMatchScore;
    }

    public int getExperienceMatchScore() {
        return experienceMatchScore;
    }

    public void setExperienceMatchScore(int experienceMatchScore) {
        this.experienceMatchScore = experienceMatchScore;
    }

    public String getFitCategory() {
        return fitCategory;
    }

    public void setFitCategory(String fitCategory) {
        this.fitCategory = fitCategory;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public LocalDateTime getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(LocalDateTime matchedAt) {
        this.matchedAt = matchedAt;
    }
}
