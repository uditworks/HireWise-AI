package com.hirewise.hirewise_backend.service.ai;

import com.hirewise.hirewise_backend.dto.JobMatchResponse;
import com.hirewise.hirewise_backend.dto.ResumeAnalysisResponse;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.Resume;

public interface AiResumeAnalyzerService {

    ResumeAnalysisResponse analyzeResume(Resume resume);

    JobMatchResponse matchResumeToJob(Resume resume, Job job);
}
