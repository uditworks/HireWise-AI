package com.hirewise.hirewise_backend.service;

import com.hirewise.hirewise_backend.dto.DownloadedResume;
import com.hirewise.hirewise_backend.dto.JobMatchResponse;
import com.hirewise.hirewise_backend.dto.ResumeAnalysisResponse;
import com.hirewise.hirewise_backend.dto.ResumeResponse;
import com.hirewise.hirewise_backend.dto.ResumeSummaryResponse;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobStatus;
import com.hirewise.hirewise_backend.entity.Resume;
import com.hirewise.hirewise_backend.entity.ResumeProcessingStatus;
import com.hirewise.hirewise_backend.entity.Role;
import com.hirewise.hirewise_backend.entity.User;
import com.hirewise.hirewise_backend.exception.ResourceNotFoundException;
import com.hirewise.hirewise_backend.repository.JobApplicationRepository;
import com.hirewise.hirewise_backend.repository.JobRepository;
import com.hirewise.hirewise_backend.repository.ResumeRepository;
import com.hirewise.hirewise_backend.repository.UserRepository;
import com.hirewise.hirewise_backend.service.ai.AiResumeAnalyzerService;
import com.hirewise.hirewise_backend.service.pdf.PdfTextExtractorService;
import com.hirewise.hirewise_backend.service.storage.StorageService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResumeServiceImpl implements ResumeService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final StorageService storageService;
    private final PdfTextExtractorService pdfTextExtractorService;
    private final AiResumeAnalyzerService aiResumeAnalyzerService;

    public ResumeServiceImpl(ResumeRepository resumeRepository,
                             UserRepository userRepository,
                             JobRepository jobRepository,
                             JobApplicationRepository jobApplicationRepository,
                             StorageService storageService,
                             PdfTextExtractorService pdfTextExtractorService,
                             AiResumeAnalyzerService aiResumeAnalyzerService) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.storageService = storageService;
        this.pdfTextExtractorService = pdfTextExtractorService;
        this.aiResumeAnalyzerService = aiResumeAnalyzerService;
    }

    @Override
    public ResumeResponse uploadResume(MultipartFile file, String seekerEmail) {
        User seeker = getAuthenticatedSeeker(seekerEmail);
        byte[] fileBytes = validateAndGetFileBytes(file);

        // Validate PDF structure and extract text
        String extractedText = pdfTextExtractorService.extractText(fileBytes);

        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String storageIdentifier = storageService.store(fileBytes, originalFilename);

        Resume resume = new Resume(
                seeker,
                originalFilename,
                storageIdentifier,
                (long) fileBytes.length,
                "application/pdf"
        );
        resume.setExtractedText(extractedText);
        resume.setProcessingStatus(ResumeProcessingStatus.PARSED);

        try {
            Resume saved = resumeRepository.save(resume);
            return ResumeResponse.fromEntity(saved);
        } catch (Exception e) {
            // Clean up stored file if database operation fails
            storageService.delete(storageIdentifier);
            throw new IllegalStateException("Failed to persist resume record", e);
        }
    }

    @Override
    public ResumeResponse replaceResume(Long id, MultipartFile file, String seekerEmail) {
        User seeker = getAuthenticatedSeeker(seekerEmail);
        Resume resume = resumeRepository.findByIdAndOwnerId(id, seeker.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));

        byte[] fileBytes = validateAndGetFileBytes(file);
        String extractedText = pdfTextExtractorService.extractText(fileBytes);

        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        // Store new file FIRST before touching old file or record
        String newStorageId = storageService.store(fileBytes, originalFilename);
        String oldStorageId = resume.getStorageIdentifier();

        resume.setOriginalFilename(originalFilename);
        resume.setStorageIdentifier(newStorageId);
        resume.setFileSize((long) fileBytes.length);
        resume.setContentType("application/pdf");
        resume.setExtractedText(extractedText);
        resume.setProcessingStatus(ResumeProcessingStatus.PARSED);

        try {
            Resume updated = resumeRepository.save(resume);
            // Safe replacement: delete old file ONLY after new file and record are safely saved
            if (oldStorageId != null && !oldStorageId.equals(newStorageId)) {
                storageService.delete(oldStorageId);
            }
            return ResumeResponse.fromEntity(updated);
        } catch (Exception e) {
            // Rollback stored new file on error
            storageService.delete(newStorageId);
            throw new IllegalStateException("Failed to update resume record", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeSummaryResponse> getMyResumes(String seekerEmail) {
        User seeker = getAuthenticatedSeeker(seekerEmail);
        List<Resume> resumes = resumeRepository.findByOwnerIdOrderByCreatedAtDesc(seeker.getId());
        return resumes.stream()
                .map(ResumeSummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getResumeById(Long id, String currentUserEmail) {
        User user = getUser(currentUserEmail);
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));

        validateAccess(resume, user);
        return ResumeResponse.fromEntity(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedResume downloadResume(Long id, String currentUserEmail) {
        User user = getUser(currentUserEmail);
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));

        validateAccess(resume, user);

        byte[] data = storageService.load(resume.getStorageIdentifier());
        return new DownloadedResume(data, resume.getOriginalFilename(), resume.getContentType());
    }

    @Override
    public void deleteResume(Long id, String seekerEmail) {
        User seeker = getAuthenticatedSeeker(seekerEmail);
        Resume resume = resumeRepository.findByIdAndOwnerId(id, seeker.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));

        // 1. Safely decouple from applications without deleting application records
        jobApplicationRepository.nullifyResumeReferences(resume.getId());

        // 2. Delete file from storage
        storageService.delete(resume.getStorageIdentifier());

        // 3. Delete database record
        resumeRepository.delete(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeAnalysisResponse analyzeResume(Long id, String seekerEmail) {
        User seeker = getAuthenticatedSeeker(seekerEmail);
        Resume resume = resumeRepository.findByIdAndOwnerId(id, seeker.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));

        return aiResumeAnalyzerService.analyzeResume(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public JobMatchResponse matchResumeToJob(Long id, Long jobId, String currentUserEmail) {
        User user = getUser(currentUserEmail);
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // Authorization checks
        if (user.getRole() == Role.JOB_SEEKER) {
            if (!resume.getOwner().getId().equals(user.getId())) {
                throw new AccessDeniedException("You can only match your own resumes");
            }
            if (job.getStatus() != JobStatus.PUBLISHED) {
                throw new IllegalArgumentException("Job matching is only available for published jobs");
            }
        } else if (user.getRole() == Role.RECRUITER) {
            if (!job.getRecruiter().getId().equals(user.getId())) {
                throw new AccessDeniedException("Recruiters can only match candidates against their own jobs");
            }
            validateRecruiterAccessToResume(resume, user);
        } else {
            throw new AccessDeniedException("Unauthorized to perform job matching");
        }

        return aiResumeAnalyzerService.matchResumeToJob(resume, job);
    }

    private User getAuthenticatedSeeker(String email) {
        User user = getUser(email);
        if (user.getRole() != Role.JOB_SEEKER) {
            throw new AccessDeniedException("Only job seekers can perform resume management operations");
        }
        return user;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private void validateAccess(Resume resume, User user) {
        if (user.getRole() == Role.JOB_SEEKER) {
            if (!resume.getOwner().getId().equals(user.getId())) {
                throw new ResourceNotFoundException("Resume not found with id: " + resume.getId());
            }
            return;
        }
        if (user.getRole() == Role.RECRUITER) {
            validateRecruiterAccessToResume(resume, user);
            return;
        }
        throw new AccessDeniedException("Unauthorized to access this resume");
    }

    private void validateRecruiterAccessToResume(Resume resume, User recruiter) {
        boolean hasApplication = jobApplicationRepository.existsByResumeIdAndJobRecruiterId(resume.getId(), recruiter.getId())
                || jobApplicationRepository.existsByApplicantIdAndJobRecruiterId(resume.getOwner().getId(), recruiter.getId());

        if (!hasApplication) {
            throw new AccessDeniedException("You do not have permission to access this candidate's resume");
        }
    }

    private byte[] validateAndGetFileBytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of 10 MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Invalid file format. Only PDF files are accepted.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.equalsIgnoreCase("application/pdf") && !contentType.equalsIgnoreCase("application/octet-stream")) {
            throw new IllegalArgumentException("Invalid content type. Only PDF files are accepted.");
        }

        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read uploaded file contents", e);
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "resume.pdf";
        }
        String clean = Paths.get(filename).getFileName().toString();
        clean = clean.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!clean.toLowerCase().endsWith(".pdf")) {
            clean += ".pdf";
        }
        return clean;
    }
}
