package com.hirewise.hirewise_backend.service.storage;

public interface StorageService {

    String store(byte[] content, String originalFilename);

    byte[] load(String storageIdentifier);

    void delete(String storageIdentifier);
}
