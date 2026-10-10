package com.hirewise.hirewise_backend.service.storage;

import com.hirewise.hirewise_backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private FileSystemStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        storageService = new FileSystemStorageServiceImpl(tempDir.toString());
        storageService.init();
    }

    @AfterEach
    void tearDown() {
        // Clean up temp dir contents if needed
    }

    @Test
    void store_and_load_success() {
        byte[] content = "Dummy PDF Content %PDF-1.4".getBytes();
        String originalFilename = "test_resume.pdf";

        String storageId = storageService.store(content, originalFilename);

        assertNotNull(storageId);
        assertTrue(storageId.endsWith(".pdf"));

        byte[] loaded = storageService.load(storageId);
        assertArrayEquals(content, loaded);
    }

    @Test
    void store_emptyContent_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            storageService.store(new byte[0], "empty.pdf");
        });
    }

    @Test
    void load_nonExistentFile_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            storageService.load("00000000-0000-0000-0000-000000000000.pdf");
        });
    }

    @Test
    void delete_existingFile_deletesSuccessfully() {
        byte[] content = "Data to delete".getBytes();
        String storageId = storageService.store(content, "delete_me.pdf");

        assertTrue(Files.exists(storageService.getRootLocation().resolve(storageId)));

        storageService.delete(storageId);

        assertFalse(Files.exists(storageService.getRootLocation().resolve(storageId)));
    }

    @Test
    void delete_nonExistentOrNull_doesNotThrow() {
        assertDoesNotThrow(() -> storageService.delete(null));
        assertDoesNotThrow(() -> storageService.delete(""));
        assertDoesNotThrow(() -> storageService.delete("nonexistent-file.pdf"));
    }

    @Test
    void pathTraversal_throwsSecurityException() {
        assertThrows(SecurityException.class, () -> {
            storageService.load("../../../etc/passwd");
        });
        assertThrows(SecurityException.class, () -> {
            storageService.load("..\\..\\secret.txt");
        });
    }

    @Test
    void init_withNullOrEmptyDir_defaultsAndInitializes() {
        FileSystemStorageServiceImpl defaultStorage = new FileSystemStorageServiceImpl(null);
        assertDoesNotThrow(defaultStorage::init);
        assertNotNull(defaultStorage.getRootLocation());
    }
}
