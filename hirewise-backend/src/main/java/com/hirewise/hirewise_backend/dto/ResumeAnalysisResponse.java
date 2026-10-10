package com.hirewise.hirewise_backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class ResumeAnalysisResponse {

    private Long resumeId;
    private String summary;
    private Set<String> detectedSkills;
    private List<String> strengths;
    private List<String> gaps;
    private List<String> improvementSuggestions;
    private String disclaimer = "AI-generated resume analysis is an advisory estimate for candidate improvement and does not guarantee employment.";
    private LocalDateTime analyzedAt;

    public ResumeAnalysisResponse() {
        this.analyzedAt = LocalDateTime.now();
    }

    public ResumeAnalysisResponse(Long resumeId, String summary, Set<String> detectedSkills,
                                  List<String> strengths, List<String> gaps,
                                  List<String> improvementSuggestions) {
        this.resumeId = resumeId;
        this.summary = summary;
        this.detectedSkills = detectedSkills;
        this.strengths = strengths;
        this.gaps = gaps;
        this.improvementSuggestions = improvementSuggestions;
        this.analyzedAt = LocalDateTime.now();
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Set<String> getDetectedSkills() {
        return detectedSkills;
    }

    public void setDetectedSkills(Set<String> detectedSkills) {
        this.detectedSkills = detectedSkills;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getGaps() {
        return gaps;
    }

    public void setGaps(List<String> gaps) {
        this.gaps = gaps;
    }

    public List<String> getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(List<String> improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}
