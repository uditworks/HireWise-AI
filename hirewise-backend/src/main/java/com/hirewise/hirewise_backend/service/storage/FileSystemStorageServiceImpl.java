package com.hirewise.hirewise_backend.service.storage;

import com.hirewise.hirewise_backend.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
public class FileSystemStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(FileSystemStorageServiceImpl.class);

    private Path rootLocation;

    public FileSystemStorageServiceImpl(@Value("${hirewise.storage.resume-dir:${RESUME_STORAGE_DIR:./storage/resumes}}") String storageDir) {
        String dir = (storageDir == null || storageDir.trim().isEmpty()) ? "./storage/resumes" : storageDir.trim();
        this.rootLocation = Paths.get(dir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            if (!Files.isWritable(rootLocation)) {
                throw new IOException("Storage directory exists but is not writable: " + rootLocation);
            }
            log.info("Resume storage directory initialized successfully at: {}", rootLocation);
        } catch (Exception e) {
            log.warn("Could not initialize configured storage directory at '{}' ({}). Attempting fallback to temp directory...",
                    rootLocation, e.getMessage());
            try {
                Path fallbackPath = Paths.get(System.getProperty("java.io.tmpdir", "/tmp"), "hirewise-storage", "resumes")
                        .toAbsolutePath()
                        .normalize();
                Files.createDirectories(fallbackPath);
                if (!Files.isWritable(fallbackPath)) {
                    throw new IOException("Fallback storage directory exists but is not writable: " + fallbackPath);
                }
                this.rootLocation = fallbackPath;
                log.info("Fallback resume storage directory initialized successfully at: {}", this.rootLocation);
            } catch (Exception fallbackEx) {
                log.error("Failed to initialize both primary ({}) and fallback storage directories.", rootLocation, fallbackEx);
                throw new IllegalStateException(
                        "Could not initialize resume storage directory at '" + rootLocation + "' nor fallback directory. " +
                        "Please configure RESUME_STORAGE_DIR to a writable directory.", fallbackEx);
            }
        }
    }

    public Path getRootLocation() {
        return rootLocation;
    }

    @Override
    public String store(byte[] content, String originalFilename) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String storageIdentifier = UUID.randomUUID().toString() + ".pdf";
        Path destination = resolveAndValidatePath(storageIdentifier);

        try {
            Files.write(destination, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return storageIdentifier;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + storageIdentifier, e);
        }
    }

    @Override
    public byte[] load(String storageIdentifier) {
        Path filePath = resolveAndValidatePath(storageIdentifier);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new ResourceNotFoundException("Resume file not found or is unreadable");
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read stored file: " + storageIdentifier, e);
        }
    }

    @Override
    public void delete(String storageIdentifier) {
        if (storageIdentifier == null || storageIdentifier.isBlank()) {
            return;
        }
        try {
            Path filePath = resolveAndValidatePath(storageIdentifier);
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            log.warn("Could not delete stored resume file '{}': {}", storageIdentifier, e.getMessage());
        }
    }

    private Path resolveAndValidatePath(String storageIdentifier) {
        if (storageIdentifier == null || storageIdentifier.isBlank() ||
                storageIdentifier.contains("..") || storageIdentifier.contains("/") || storageIdentifier.contains("\\")) {
            throw new SecurityException("Invalid storage identifier");
        }

        Path destination = rootLocation.resolve(storageIdentifier).normalize();
        if (!destination.startsWith(rootLocation)) {
            throw new SecurityException("Cannot access file outside designated storage directory");
        }
        return destination;
    }
}
