package com.ian.web.employee.leave;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles secure storage and retrieval of leave application attachments.
 *
 * Security measures:
 *   - Stored filename is a random UUID (never the original filename) to prevent
 *     path traversal and filename collision attacks.
 *   - MIME type whitelist rejects executable or dangerous content types.
 *   - Upload directory is outside the webroot so files are never served statically.
 *   - Max file size enforced at the Spring multipart layer (5 MB, see properties).
 */
@Service
@Slf4j
public class FileStorageService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private final Path uploadRoot;

    public FileStorageService(@Value("${file.upload-dir:}") String uploadDir) {
        Path resolved;
        if (uploadDir == null || uploadDir.isBlank()) {
            resolved = Paths.get(System.getProperty("user.home"), "hrisp-uploads", "leave");
        } else {
            Path configured = Paths.get(uploadDir);
            // On Windows an absolute Unix-style path like /hrisp/uploads/leave starts with /
            // which maps to the current drive root. Try to create it; fall back to user home.
            resolved = configured.toAbsolutePath().normalize();
        }
        this.uploadRoot = resolved;
        try {
            Files.createDirectories(this.uploadRoot);
            log.info("Leave attachment upload directory: {}", this.uploadRoot);
        } catch (IOException e) {
            log.error("Cannot create file upload directory {}: {}", uploadRoot, e.getMessage());
            throw new RuntimeException("Could not create upload directory: " + uploadRoot, e);
        }
    }

    /**
     * Stores the uploaded file securely and returns the stored UUID filename.
     *
     * @param file           The uploaded multipart file
     * @param originalName   Original filename (stored separately for display)
     * @return UUID-based filename stored on disk (store this in attachment_path)
     * @throws IllegalArgumentException if MIME type is not allowed
     * @throws RuntimeException         if storage fails
     */
    public String store(MultipartFile file, String originalName) {
        validateMimeType(file);

        String extension = getExtension(originalName != null ? originalName : file.getOriginalFilename());
        String storedName = UUID.randomUUID().toString() + extension;
        Path target = uploadRoot.resolve(storedName).normalize();

        // Ensure resolved path is still inside the upload root (path traversal guard)
        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid file path detected.");
        }

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored leave attachment: {} → {}", originalName, storedName);
            return storedName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + storedName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Loads a stored attachment as a Spring Resource for streaming to the browser.
     *
     * @param storedFilename The UUID filename returned by store()
     * @return Resource for the file
     * @throws RuntimeException if the file does not exist or cannot be read
     */
    public Resource load(String storedFilename) {
        // Strip any directory components from the filename (path traversal guard)
        String safeFilename = Paths.get(storedFilename).getFileName().toString();
        Path filePath = uploadRoot.resolve(safeFilename).normalize();

        if (!filePath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid file path detected.");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("File not found or not readable: " + safeFilename);
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + safeFilename, e);
        }
    }

    /**
     * Deletes a stored file (e.g., when a leave application is deleted).
     * Does not throw if file is missing.
     */
    public void delete(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()) return;
        String safeFilename = Paths.get(storedFilename).getFileName().toString();
        try {
            Files.deleteIfExists(uploadRoot.resolve(safeFilename).normalize());
        } catch (IOException e) {
            log.warn("Could not delete attachment {}: {}", safeFilename, e.getMessage());
        }
    }

    /** Returns the MIME type of the uploaded file, falling back to octet-stream. */
    public String detectMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null ? contentType : "application/octet-stream";
    }

    // -----------------------------------------------------------------------

    private void validateMimeType(MultipartFile file) {
        String mime = file.getContentType();
        if (mime == null || !ALLOWED_MIME_TYPES.contains(mime.toLowerCase())) {
            throw new IllegalArgumentException(
                    "File type not allowed. Only PDF, JPEG, and PNG files may be uploaded.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : "";
    }
}
