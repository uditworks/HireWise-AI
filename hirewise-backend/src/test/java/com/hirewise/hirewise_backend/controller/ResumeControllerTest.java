package com.hirewise.hirewise_backend.controller;

import com.hirewise.hirewise_backend.dto.DownloadedResume;
import com.hirewise.hirewise_backend.dto.JobMatchResponse;
import com.hirewise.hirewise_backend.dto.ResumeAnalysisResponse;
import com.hirewise.hirewise_backend.dto.ResumeResponse;
import com.hirewise.hirewise_backend.dto.ResumeSummaryResponse;
import com.hirewise.hirewise_backend.entity.ResumeProcessingStatus;
import com.hirewise.hirewise_backend.exception.GlobalExceptionHandler;
import com.hirewise.hirewise_backend.service.ResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ResumeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ResumeService resumeService;

    @InjectMocks
    private ResumeController resumeController;

    private UsernamePasswordAuthenticationToken seekerAuth;
    private ResumeResponse sampleResumeResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(resumeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        seekerAuth = new UsernamePasswordAuthenticationToken(
                "seeker@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_JOB_SEEKER"))
        );

        sampleResumeResponse = new ResumeResponse(
                1L, 10L, "seeker@example.com", "resume.pdf", 2048L,
                "application/pdf", ResumeProcessingStatus.PARSED, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /api/resumes/upload should return 201 Created with ResumeResponse")
    void testUploadResumeEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "%PDF-1.4 test".getBytes()
        );

        when(resumeService.uploadResume(any(), eq("seeker@example.com"))).thenReturn(sampleResumeResponse);

        mockMvc.perform(multipart("/api/resumes/upload")
                        .file(file)
                        .principal(seekerAuth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.originalFilename", is("resume.pdf")))
                .andExpect(jsonPath("$.processingStatus", is("PARSED")));
    }

    @Test
    @DisplayName("PUT /api/resumes/{id} should return 200 OK with updated ResumeResponse")
    void testReplaceResumeEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "new_resume.pdf", "application/pdf", "%PDF-1.4 updated".getBytes()
        );

        when(resumeService.replaceResume(eq(1L), any(), eq("seeker@example.com"))).thenReturn(sampleResumeResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/api/resumes/1")
                        .file(file)
                        .principal(seekerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("GET /api/resumes/my should return list of resumes")
    void testGetMyResumesEndpoint() throws Exception {
        ResumeSummaryResponse summary = new ResumeSummaryResponse(
                1L, "resume.pdf", 2048L, ResumeProcessingStatus.PARSED,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(resumeService.getMyResumes("seeker@example.com")).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/resumes/my")
                        .principal(seekerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].originalFilename", is("resume.pdf")));
    }

    @Test
    @DisplayName("GET /api/resumes/{id}/download should return PDF bytes and Content-Disposition")
    void testDownloadResumeEndpoint() throws Exception {
        byte[] pdfBytes = "%PDF-1.4 sample binary".getBytes();
        DownloadedResume downloaded = new DownloadedResume(pdfBytes, "my_resume.pdf", "application/pdf");

        when(resumeService.downloadResume(eq(1L), eq("seeker@example.com"))).thenReturn(downloaded);

        mockMvc.perform(get("/api/resumes/1/download")
                        .principal(seekerAuth))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_resume.pdf\""))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    @DisplayName("DELETE /api/resumes/{id} should return 204 No Content")
    void testDeleteResumeEndpoint() throws Exception {
        doNothing().when(resumeService).deleteResume(eq(1L), eq("seeker@example.com"));

        mockMvc.perform(delete("/api/resumes/1")
                        .principal(seekerAuth))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/resumes/{id}/analyze should return ResumeAnalysisResponse")
    void testAnalyzeResumeEndpoint() throws Exception {
        ResumeAnalysisResponse analysis = new ResumeAnalysisResponse(
                1L, "Summary", Set.of("Java"), List.of("Strong"), List.of("Gaps"), List.of("Suggestions")
        );
        when(resumeService.analyzeResume(eq(1L), eq("seeker@example.com"))).thenReturn(analysis);

        mockMvc.perform(get("/api/resumes/1/analyze")
                        .principal(seekerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeId", is(1)))
                .andExpect(jsonPath("$.summary", is("Summary")))
                .andExpect(jsonPath("$.disclaimer").exists());
    }

    @Test
    @DisplayName("GET /api/resumes/{id}/match/{jobId} should return JobMatchResponse")
    void testMatchResumeToJobEndpoint() throws Exception {
        JobMatchResponse match = new JobMatchResponse(
                1L, 10L, "Java Dev", 85, 90, 80, "High Match",
                List.of("Java"), List.of(), "Fit explanation"
        );
        when(resumeService.matchResumeToJob(eq(1L), eq(10L), eq("seeker@example.com"))).thenReturn(match);

        mockMvc.perform(get("/api/resumes/1/match/10")
                        .principal(seekerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeId", is(1)))
                .andExpect(jsonPath("$.overallMatchScore", is(85)))
                .andExpect(jsonPath("$.fitCategory", is("High Match")));
    }
}
