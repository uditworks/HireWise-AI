package com.hirewise.hirewise_backend.repository;

import com.hirewise.hirewise_backend.entity.ApplicationStatus;
import com.hirewise.hirewise_backend.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    Page<JobApplication> findByApplicantId(Long applicantId, Pageable pageable);

    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);

    Page<JobApplication> findByJobIdAndStatus(Long jobId, ApplicationStatus status, Pageable pageable);

    Optional<JobApplication> findByJobIdAndApplicantId(Long jobId, Long applicantId);

    boolean existsByJobIdAndApplicantId(Long jobId, Long applicantId);

    long countByJobId(Long jobId);

    boolean existsByResumeIdAndJobRecruiterId(Long resumeId, Long recruiterId);

    boolean existsByApplicantIdAndJobRecruiterId(Long applicantId, Long recruiterId);

    @Modifying
    @Query("UPDATE JobApplication a SET a.resume = NULL WHERE a.resume.id = :resumeId")
    void nullifyResumeReferences(@Param("resumeId") Long resumeId);
}
