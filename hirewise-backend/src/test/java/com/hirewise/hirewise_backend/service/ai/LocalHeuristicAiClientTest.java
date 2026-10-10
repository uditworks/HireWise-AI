package com.hirewise.hirewise_backend.service.ai;

import com.hirewise.hirewise_backend.dto.JobMatchResponse;
import com.hirewise.hirewise_backend.dto.ResumeAnalysisResponse;
import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LocalHeuristicAiClientTest {

    private LocalHeuristicAiClient client;
    private User recruiter;
    private Job javaBackendJob;

    @BeforeEach
    void setUp() {
        client = new LocalHeuristicAiClient();

        recruiter = new User("Jane Recruiter", "jane@company.com", "hash", Role.RECRUITER);
        recruiter.setId(10L);

        javaBackendJob = new Job();
        javaBackendJob.setId(100L);
        javaBackendJob.setTitle("Senior Java Developer");
        javaBackendJob.setDescription("Building resilient backend services using Spring Boot and MySQL");
        javaBackendJob.setLocation("Remote");
        javaBackendJob.setEmploymentType(EmploymentType.FULL_TIME);
        javaBackendJob.setSalaryRange("$120,000 - $150,000");
        javaBackendJob.setRequiredSkills(Set.of("Java", "Spring Boot", "MySQL", "Docker", "AWS"));
        javaBackendJob.setRecruiter(recruiter);
    }

    @Test
    @DisplayName("Should extract technical skills accurately including boundary cases")
    void testExtractSkills() {
        String resumeText = "Experienced Software Engineer proficient in Java, Spring Boot, C++, C#, Docker, AWS, and MySQL.";

        Set<String> skills = client.extractSkills(resumeText);

        assertThat(skills).contains("Java", "Spring Boot", "C++", "C#", "Docker", "AWS", "MySQL");
        assertThat(skills).doesNotContain("PHP", "Ruby", "Python");
    }

    @Test
    @DisplayName("Should extract years of experience from patterns and date ranges")
    void testExtractExperienceYears() {
        String text1 = "Senior Developer with 6+ years of experience in distributed systems.";
        assertThat(client.extractExperienceYears(text1)).isEqualTo(6);

        String text2 = "Software Engineer (2018 - 2024) at TechCorp.";
        assertThat(client.extractExperienceYears(text2)).isEqualTo(6);
    }

    @Test
    @DisplayName("Should analyze resume and produce structured, explainable response")
    void testAnalyzeResume() {
        String resumeText = "John Doe\nSenior Backend Engineer\n5+ years of experience.\nSkills: Java, Spring Boot, MySQL, Redis, Git, Unit Testing.\nBuilt microservices architecture handling 10k RPS.";

        ResumeAnalysisResponse response = client.analyze(1L, resumeText);

        assertThat(response.getResumeId()).isEqualTo(1L);
        assertThat(response.getSummary()).contains("5+ years");
        assertThat(response.getDetectedSkills()).contains("Java", "Spring Boot", "MySQL", "Redis");
        assertThat(response.getStrengths()).isNotEmpty();
        assertThat(response.getGaps()).isNotEmpty();
        assertThat(response.getImprovementSuggestions()).isNotEmpty();
        assertThat(response.getDisclaimer()).contains("advisory estimate");
    }

    @Test
    @DisplayName("Should handle empty or scanned PDF text gracefully during analysis")
    void testAnalyzeEmptyOrScannedResume() {
        ResumeAnalysisResponse response = client.analyze(2L, "   ");

        assertThat(response.getResumeId()).isEqualTo(2L);
        assertThat(response.getSummary()).contains("image-based or scanned document");
        assertThat(response.getDetectedSkills()).isEmpty();
        assertThat(response.getImprovementSuggestions()).isNotEmpty();
    }

    @Test
    @DisplayName("Should calculate explainable job match scores with matched and missing skills")
    void testMatchResumeToJob() {
        String resumeText = "Candidate with 5 years experience. Skills: Java, Spring Boot, MySQL. Built scalable REST APIs.";

        JobMatchResponse response = client.match(1L, resumeText, javaBackendJob);

        assertThat(response.getResumeId()).isEqualTo(1L);
        assertThat(response.getJobId()).isEqualTo(100L);
        assertThat(response.getJobTitle()).isEqualTo("Senior Java Developer");
        assertThat(response.getMatchedSkills()).contains("Java", "Spring Boot", "MySQL");
        assertThat(response.getMissingSkills()).contains("Docker", "AWS");
        assertThat(response.getSkillMatchScore()).isEqualTo(60); // 3 of 5 = 60%
        assertThat(response.getExperienceMatchScore()).isEqualTo(100); // 5 of 5 = 100%
        assertThat(response.getOverallMatchScore()).isEqualTo(76); // 0.6 * 60 + 0.4 * 100 = 76
        assertThat(response.getFitCategory()).isEqualTo("High Match");
        assertThat(response.getExplanation()).contains("Matched 3 of 5");
        assertThat(response.getDisclaimer()).contains("algorithmic estimates");
    }

    @Test
    @DisplayName("Should give Low Match when candidate skills are completely missing")
    void testLowMatchJob() {
        String resumeText = "Graphic designer specializing in Photoshop and Figma with 1 year experience.";

        JobMatchResponse response = client.match(1L, resumeText, javaBackendJob);

        assertThat(response.getMatchedSkills()).isEmpty();
        assertThat(response.getMissingSkills()).contains("Java", "Spring Boot", "MySQL", "Docker", "AWS");
        assertThat(response.getSkillMatchScore()).isEqualTo(0);
        assertThat(response.getFitCategory()).isEqualTo("Low Match");
    }
}
