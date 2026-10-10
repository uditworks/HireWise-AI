package com.hirewise.hirewise_backend.service.ai;

import com.hirewise.hirewise_backend.dto.JobMatchResponse;
import com.hirewise.hirewise_backend.dto.ResumeAnalysisResponse;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.Resume;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiResumeAnalyzerServiceImpl implements AiResumeAnalyzerService {

    private final String provider;
    private final AiResumeClient localClient;
    private final AiResumeClient externalClient;

    public AiResumeAnalyzerServiceImpl(
            @Value("${hirewise.ai.provider:local}") String provider,
            @Qualifier("localAiClient") AiResumeClient localClient,
            @Qualifier("externalAiClient") AiResumeClient externalClient) {
        this.provider = provider != null ? provider.trim().toLowerCase() : "local";
        this.localClient = localClient;
        this.externalClient = externalClient;
    }

    @Override
    public ResumeAnalysisResponse analyzeResume(Resume resume) {
        if (resume == null) {
            throw new IllegalArgumentException("Resume cannot be null");
        }
        AiResumeClient client = selectClient();
        return client.analyze(resume.getId(), resume.getExtractedText());
    }

    @Override
    public JobMatchResponse matchResumeToJob(Resume resume, Job job) {
        if (resume == null) {
            throw new IllegalArgumentException("Resume cannot be null");
        }
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null");
        }
        AiResumeClient client = selectClient();
        return client.match(resume.getId(), resume.getExtractedText(), job);
    }

    private AiResumeClient selectClient() {
        if ("gemini".equalsIgnoreCase(provider) || "openai".equalsIgnoreCase(provider)) {
            return externalClient;
        }
        return localClient;
    }
}
