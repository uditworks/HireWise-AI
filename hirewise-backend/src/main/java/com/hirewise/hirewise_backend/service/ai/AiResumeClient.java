package com.hirewise.hirewise_backend.service.ai;

import com.hirewise.hirewise_backend.dto.JobMatchResponse;
import com.hirewise.hirewise_backend.dto.ResumeAnalysisResponse;
import com.hirewise.hirewise_backend.entity.Job;

public interface AiResumeClient {

    ResumeAnalysisResponse analyze(Long resumeId, String resumeText);

    JobMatchResponse match(Long resumeId, String resumeText, Job job);
}
