package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.DownloadedResume;
import com.hirewise.hirewise_backend.dto.JobMatchResponse;
import com.hirewise.hirewise_backend.dto.ResumeAnalysisResponse;
import com.hirewise.hirewise_backend.dto.ResumeResponse;
import com.hirewise.hirewise_backend.dto.ResumeSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {

    ResumeResponse uploadResume(MultipartFile file, String seekerEmail);

    ResumeResponse replaceResume(Long id, MultipartFile file, String seekerEmail);

    List<ResumeSummaryResponse> getMyResumes(String seekerEmail);

    ResumeResponse getResumeById(Long id, String currentUserEmail);

    DownloadedResume downloadResume(Long id, String currentUserEmail);

    void deleteResume(Long id, String seekerEmail);

    ResumeAnalysisResponse analyzeResume(Long id, String seekerEmail);

    JobMatchResponse matchResumeToJob(Long id, Long jobId, String currentUserEmail);
}
