package com.hirewise.hirewise_backend.controller;

import com.hirewise.hirewise_backend.dto.JobCreateRequest;
import com.hirewise.hirewise_backend.dto.JobResponse;
import com.hirewise.hirewise_backend.dto.RecruiterSummaryResponse;
import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.JobStatus;
import com.hirewise.hirewise_backend.exception.GlobalExceptionHandler;
import com.hirewise.hirewise_backend.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(jobController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("GET /api/jobs returns 200 OK with paginated list of published jobs")
    void getPublishedJobs_Returns200() throws Exception {
        JobResponse response = new JobResponse(
                1L, "Software Engineer", "Build backend systems", "Remote",
                EmploymentType.FULL_TIME, "$100k-$130k", Set.of("Java", "Spring"),
                JobStatus.PUBLISHED, new RecruiterSummaryResponse(2L, "Jane", "jane@company.com"),
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(jobService.getPublishedJobs(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/jobs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("Software Engineer")))
                .andExpect(jsonPath("$.content[0].recruiter.email", is("jane@company.com")));
    }

    @Test
    @DisplayName("POST /api/jobs with invalid input returns 400 Bad Request with validation errors")
    void createJob_InvalidInput_Returns400() throws Exception {
        String invalidPayload = """
                {
                    "title": "",
                    "description": "short",
                    "location": "",
                    "employmentType": null,
                    "requiredSkills": []
                }
                """;

        mockMvc.perform(post("/api/jobs")
                        .principal(new UsernamePasswordAuthenticationToken("jane@company.com", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.details.title").exists())
                .andExpect(jsonPath("$.details.description").exists())
                .andExpect(jsonPath("$.details.location").exists())
                .andExpect(jsonPath("$.details.employmentType").exists())
                .andExpect(jsonPath("$.details.requiredSkills").exists());
    }

    @Test
    @DisplayName("POST /api/jobs with valid input returns 201 Created")
    void createJob_ValidInput_Returns201() throws Exception {
        String validPayload = """
                {
                    "title": "Lead Software Engineer",
                    "description": "Comprehensive engineering position details with sufficient characters",
                    "location": "San Francisco, CA",
                    "employmentType": "FULL_TIME",
                    "salaryRange": "$180,000 - $220,000",
                    "requiredSkills": ["Java", "Distributed Systems"]
                }
                """;

        JobResponse response = new JobResponse(
                5L, "Lead Software Engineer", "Description", "San Francisco, CA",
                EmploymentType.FULL_TIME, "$180,000 - $220,000", Set.of("Java"),
                JobStatus.DRAFT, new RecruiterSummaryResponse(2L, "Jane", "jane@company.com"),
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(jobService.createJob(any(JobCreateRequest.class), eq("jane@company.com")))
                .thenReturn(response);

        mockMvc.perform(post("/api/jobs")
                        .principal(new UsernamePasswordAuthenticationToken("jane@company.com", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.title", is("Lead Software Engineer")))
                .andExpect(jsonPath("$.status", is("DRAFT")));
    }
}
