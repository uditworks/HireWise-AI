package com.hirewise.hirewise_backend.repository;

import com.hirewise.hirewise_backend.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Resume> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<Resume> findByStorageIdentifier(String storageIdentifier);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}
