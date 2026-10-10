package com.hirewise.hirewise_backend.repository;

import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Page<Job> findByRecruiterId(Long recruiterId, Pageable pageable);

    Page<Job> findByRecruiterEmail(String email, Pageable pageable);

    Optional<Job> findByIdAndStatus(Long id, JobStatus status);
}
