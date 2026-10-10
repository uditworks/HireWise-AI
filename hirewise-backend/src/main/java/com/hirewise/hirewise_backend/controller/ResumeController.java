package com.hirewise.hirewise_backend.controller;

import com.hirewise.hirewise_backend.dto.DownloadedResume;
import com.hirewise.hirewise_backend.dto.JobMatchResponse;
import com.hirewise.hirewise_backend.dto.ResumeAnalysisResponse;
import com.hirewise.hirewise_backend.dto.ResumeResponse;
import com.hirewise.hirewise_backend.dto.ResumeSummaryResponse;
import com.hirewise.hirewise_backend.service.ResumeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        ResumeResponse response = resumeService.uploadResume(file, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ResumeResponse> replaceResume(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        ResumeResponse response = resumeService.replaceResume(id, file, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<ResumeSummaryResponse>> getMyResumes(Authentication authentication) {
        List<ResumeSummaryResponse> resumes = resumeService.getMyResumes(authentication.getName());
        return ResponseEntity.ok(resumes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResumeResponse> getResumeById(
            @PathVariable Long id,
            Authentication authentication) {
        ResumeResponse response = resumeService.getResumeById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadResume(
            @PathVariable Long id,
            Authentication authentication) {
        DownloadedResume downloaded = resumeService.downloadResume(id, authentication.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloaded.getFilename() + "\"")
                .body(downloaded.getContent());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long id,
            Authentication authentication) {
        resumeService.deleteResume(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/analyze")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(
            @PathVariable Long id,
            Authentication authentication) {
        ResumeAnalysisResponse response = resumeService.analyzeResume(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/match/{jobId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobMatchResponse> matchResumeToJob(
            @PathVariable Long id,
            @PathVariable Long jobId,
            Authentication authentication) {
        JobMatchResponse response = resumeService.matchResumeToJob(id, jobId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
