package com.hirewise.hirewise_backend.service.storage;

import com.hirewise.hirewise_backend.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
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

    private final Path rootLocation;

    public FileSystemStorageServiceImpl(@Value("${hirewise.storage.resume-dir:./storage/resumes}") String storageDir) {
        this.rootLocation = Paths.get(storageDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize storage directory", e);
        }
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
            throw new IllegalStateException("Failed to store file", e);
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
            throw new IllegalStateException("Failed to read stored file", e);
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
        } catch (Exception ignored) {
            // Logged or handled quietly without throwing to ensure database cleanup proceeds
        }
    }

    private Path resolveAndValidatePath(String storageIdentifier) {
        if (storageIdentifier == null || storageIdentifier.contains("..") || storageIdentifier.contains("/") || storageIdentifier.contains("\\")) {
            throw new SecurityException("Invalid storage identifier");
        }

        Path destination = rootLocation.resolve(storageIdentifier).normalize();
        if (!destination.startsWith(rootLocation)) {
            throw new SecurityException("Cannot access file outside designated storage directory");
        }
        return destination;
    }
}
