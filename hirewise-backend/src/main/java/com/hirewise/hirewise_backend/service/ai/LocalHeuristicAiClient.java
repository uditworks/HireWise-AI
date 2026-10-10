package com.hirewise.hirewise_backend.service.ai;

import com.hirewise.hirewise_backend.dto.JobMatchResponse;
import com.hirewise.hirewise_backend.dto.ResumeAnalysisResponse;
import com.hirewise.hirewise_backend.entity.Job;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("localAiClient")
public class LocalHeuristicAiClient implements AiResumeClient {

    private static final List<String> SKILL_DICTIONARY = List.of(
            // Languages
            "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "C", "Go", "Golang", "Rust",
            "Ruby", "PHP", "Kotlin", "Swift", "Dart", "Scala", "R", "SQL", "HTML", "CSS",
            // Frameworks & Libraries
            "Spring", "Spring Boot", "React", "Angular", "Vue", "Node.js", "Express", "Django",
            "Flask", "FastAPI", "ASP.NET", ".NET", "Next.js", "Redux", "Tailwind", "Bootstrap",
            "Rails", "Flutter", "Hibernate", "JPA",
            // Databases
            "MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "SQLite", "Cassandra", "Elasticsearch",
            // Cloud & DevOps
            "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Git", "GitHub", "GitLab", "CI/CD",
            "Jenkins", "Terraform", "Ansible", "Linux", "Unix",
            // Architecture & Concepts
            "REST API", "REST", "GraphQL", "Microservices", "Kafka", "RabbitMQ", "Agile", "Scrum",
            "JIRA", "Unit Testing", "TDD", "JUnit", "Mockito", "Security", "OAuth", "JWT"
    );

    @Override
    public ResumeAnalysisResponse analyze(Long resumeId, String resumeText) {
        if (resumeText == null || resumeText.trim().length() < 20) {
            return new ResumeAnalysisResponse(
                    resumeId,
                    "The uploaded PDF contains little or no machine-readable text. It appears to be an image-based or scanned document.",
                    Collections.emptySet(),
                    List.of("Document successfully uploaded and stored."),
                    List.of("No extractable text detected. Heuristic analyzer cannot process non-selectable text."),
                    List.of(
                            "Export your resume directly from Microsoft Word, Google Docs, or LaTeX as a text-based PDF.",
                            "Avoid scanning printed documents without optical character recognition (OCR)."
                    )
            );
        }

        Set<String> detectedSkills = extractSkills(resumeText);
        int estimatedYears = extractExperienceYears(resumeText);

        String summary = buildSummary(detectedSkills, estimatedYears);
        List<String> strengths = buildStrengths(detectedSkills, estimatedYears, resumeText);
        List<String> gaps = buildGaps(detectedSkills, resumeText);
        List<String> suggestions = buildSuggestions(detectedSkills, resumeText);

        return new ResumeAnalysisResponse(
                resumeId,
                summary,
                detectedSkills,
                strengths,
                gaps,
                suggestions
        );
    }

    @Override
    public JobMatchResponse match(Long resumeId, String resumeText, Job job) {
        String safeResumeText = resumeText != null ? resumeText : "";
        Set<String> candidateSkills = extractSkills(safeResumeText);
        int candidateExperience = extractExperienceYears(safeResumeText);
        Set<String> requiredSkillsSet = job.getRequiredSkills() != null ? job.getRequiredSkills() : Collections.emptySet();
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        if (requiredSkillsSet.isEmpty()) {
            // Check against job title & description
            Set<String> jobSkills = extractSkills(job.getTitle() + " " + job.getDescription());
            for (String s : jobSkills) {
                if (containsSkill(safeResumeText, candidateSkills, s)) {
                    matchedSkills.add(s);
                } else {
                    missingSkills.add(s);
                }
            }
        } else {
            for (String required : requiredSkillsSet) {
                if (containsSkill(safeResumeText, candidateSkills, required)) {
                    matchedSkills.add(required);
                } else {
                    missingSkills.add(required);
                }
            }
        }

        int totalSkills = matchedSkills.size() + missingSkills.size();
        int skillMatchScore;
        if (totalSkills == 0) {
            skillMatchScore = 80; // neutral default when no skills required
        } else {
            skillMatchScore = Math.round(((float) matchedSkills.size() / totalSkills) * 100);
        }

        int requiredExp = estimateRequiredExperience(job.getTitle(), job.getDescription());
        int experienceMatchScore = calculateExperienceScore(candidateExperience, requiredExp);

        int overallMatchScore = Math.round(0.6f * skillMatchScore + 0.4f * experienceMatchScore);
        overallMatchScore = Math.max(0, Math.min(100, overallMatchScore));

        String fitCategory;
        if (overallMatchScore >= 75) {
            fitCategory = "High Match";
        } else if (overallMatchScore >= 50) {
            fitCategory = "Moderate Match";
        } else {
            fitCategory = "Low Match";
        }

        String explanation = String.format(
                "Matched %d of %d identified job skills (%d%%). Estimated candidate experience: %d yrs (target: %d yrs, score: %d%%). Overall fit: %s.",
                matchedSkills.size(),
                totalSkills,
                skillMatchScore,
                candidateExperience,
                requiredExp,
                experienceMatchScore,
                fitCategory
        );

        return new JobMatchResponse(
                resumeId,
                job.getId(),
                job.getTitle(),
                overallMatchScore,
                skillMatchScore,
                experienceMatchScore,
                fitCategory,
                matchedSkills,
                missingSkills,
                explanation
        );
    }

    public Set<String> extractSkills(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptySet();
        }
        Set<String> matches = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String skill : SKILL_DICTIONARY) {
            if (matchesSkill(text, skill)) {
                matches.add(skill);
            }
        }
        return matches;
    }

    private boolean matchesSkill(String text, String skill) {
        String escaped = Pattern.quote(skill);
        String regex;
        if (skill.equalsIgnoreCase("C++")) {
            regex = "(?i)(?:^|[^a-zA-Z0-9])C\\+\\+(?:[^a-zA-Z0-9]|$)";
        } else if (skill.equalsIgnoreCase("C#")) {
            regex = "(?i)(?:^|[^a-zA-Z0-9])C#(?:[^a-zA-Z0-9]|$)";
        } else if (skill.equalsIgnoreCase(".NET")) {
            regex = "(?i)(?:^|[^a-zA-Z0-9])\\.NET(?:[^a-zA-Z0-9]|$)";
        } else if (skill.equalsIgnoreCase("C")) {
            regex = "(?i)(?:^|[^a-zA-Z0-9])C(?:[^a-zA-Z0-9]|$)";
        } else {
            regex = "(?i)\\b" + escaped + "\\b";
        }
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(text).find();
    }

    private boolean containsSkill(String text, Set<String> candidateSkills, String skill) {
        String trimmed = skill.trim();
        if (candidateSkills.stream().anyMatch(s -> s.equalsIgnoreCase(trimmed))) {
            return true;
        }
        return matchesSkill(text, trimmed);
    }

    public int extractExperienceYears(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        Pattern pattern = Pattern.compile("(?i)(\\d{1,2})\\+?\\s*(?:years?|yrs?)(?:\\s+of)?\\s*(?:experience|exp)?");
        Matcher matcher = pattern.matcher(text);
        int maxYears = 0;
        while (matcher.find()) {
            try {
                int years = Integer.parseInt(matcher.group(1));
                if (years > 0 && years <= 40) {
                    maxYears = Math.max(maxYears, years);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // If no explicit "X years" mentioned, scan for year ranges like 2019-2024
        if (maxYears == 0) {
            Pattern rangePattern = Pattern.compile("(?i)(20\\d{2}|19\\d{2})\\s*[-–—to]+\\s*(20\\d{2}|present|current|now)");
            Matcher rangeMatcher = rangePattern.matcher(text);
            int currentYear = 2026;
            while (rangeMatcher.find()) {
                try {
                    int startYear = Integer.parseInt(rangeMatcher.group(1));
                    String endStr = rangeMatcher.group(2).toLowerCase(Locale.ROOT);
                    int endYear = (endStr.contains("present") || endStr.contains("current") || endStr.contains("now"))
                            ? currentYear : Integer.parseInt(endStr);
                    if (endYear >= startYear && startYear >= 1990) {
                        maxYears = Math.max(maxYears, endYear - startYear);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return maxYears;
    }

    private List<String> parseRequiredSkills(String skillsString) {
        if (skillsString == null || skillsString.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(skillsString.split("[,;\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private int estimateRequiredExperience(String title, String description) {
        String combined = (title != null ? title : "") + " " + (description != null ? description : "");
        int detected = extractExperienceYears(combined);
        if (detected > 0) {
            return detected;
        }

        String lowerTitle = (title != null ? title : "").toLowerCase(Locale.ROOT);
        if (lowerTitle.contains("principal") || lowerTitle.contains("staff") || lowerTitle.contains("director")) {
            return 8;
        }
        if (lowerTitle.contains("lead") || lowerTitle.contains("architect")) {
            return 6;
        }
        if (lowerTitle.contains("senior") || lowerTitle.contains("sr")) {
            return 5;
        }
        if (lowerTitle.contains("mid") || lowerTitle.contains("intermediate")) {
            return 3;
        }
        if (lowerTitle.contains("junior") || lowerTitle.contains("entry") || lowerTitle.contains("associate") || lowerTitle.contains("intern")) {
            return 1;
        }
        return 2; // general default
    }

    private int calculateExperienceScore(int candidateYears, int requiredYears) {
        if (requiredYears <= 0) {
            return 100;
        }
        if (candidateYears >= requiredYears) {
            return 100;
        }
        if (candidateYears == 0) {
            return 50;
        }
        return Math.min(100, Math.round(((float) candidateYears / requiredYears) * 100));
    }

    private String buildSummary(Set<String> skills, int years) {
        StringBuilder sb = new StringBuilder();
        sb.append("Candidate profile with ");
        if (years > 0) {
            sb.append("approximately ").append(years).append("+ years of relevant industry experience and ");
        }
        sb.append("demonstrated proficiency in ");
        if (!skills.isEmpty()) {
            sb.append(String.join(", ", skills.stream().limit(8).collect(Collectors.toList())));
        } else {
            sb.append("general software development and technical problem solving");
        }
        sb.append(".");
        return sb.toString();
    }

    private List<String> buildStrengths(Set<String> skills, int years, String text) {
        List<String> strengths = new ArrayList<>();
        if (!skills.isEmpty()) {
            strengths.add("Strong technical foundation with detected competencies: " +
                    String.join(", ", skills.stream().limit(6).collect(Collectors.toList())));
        }
        if (years > 0) {
            strengths.add("Documented professional track record spanning " + years + "+ years.");
        }
        if (text.toLowerCase(Locale.ROOT).contains("project") || text.toLowerCase(Locale.ROOT).contains("built") || text.toLowerCase(Locale.ROOT).contains("developed")) {
            strengths.add("Active project delivery and practical software engineering contributions.");
        }
        if (strengths.isEmpty()) {
            strengths.add("Clean resume structure and readable presentation.");
        }
        return strengths;
    }

    private List<String> buildGaps(Set<String> skills, String text) {
        List<String> gaps = new ArrayList<>();
        String lower = text.toLowerCase(Locale.ROOT);

        if (!skills.contains("Docker") && !skills.contains("Kubernetes") && !lower.contains("container")) {
            gaps.add("Containerization & Cloud: Consider detailing experience with Docker, Kubernetes, or container orchestration.");
        }
        if (!skills.contains("CI/CD") && !skills.contains("Jenkins") && !lower.contains("pipeline") && !lower.contains("actions")) {
            gaps.add("DevOps & Automation: Mentioning CI/CD pipelines (e.g. GitHub Actions, Jenkins) strengthens engineering resumes.");
        }
        if (!skills.contains("AWS") && !skills.contains("Azure") && !skills.contains("GCP") && !lower.contains("cloud")) {
            gaps.add("Cloud Platforms: Highlight AWS, Azure, or GCP infrastructure experience if applicable.");
        }
        if (!lower.contains("%") && !lower.contains("increased") && !lower.contains("decreased") && !lower.contains("reduced")) {
            gaps.add("Quantifiable Impact: Add measurable metrics (e.g., 'reduced latency by 30%', 'scaled to 10k DAU') to validate achievements.");
        }

        if (gaps.isEmpty()) {
            gaps.add("No major technical gaps detected; keep updating with recent project accomplishments and new certifications.");
        }
        return gaps;
    }

    private List<String> buildSuggestions(Set<String> skills, String text) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Ensure core technical competencies are grouped logically into a dedicated 'Skills' section.");
        suggestions.add("Tailor your resume keywords to match specific job posting requirements before submitting applications.");
        suggestions.add("Use strong action verbs (e.g. Architected, Engineered, Optimized) at the start of each bullet point.");
        return suggestions;
    }
}
