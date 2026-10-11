package com.abc.expensevoucher.service;

import com.abc.expensevoucher.config.AppProperties;
import com.abc.expensevoucher.exception.FileValidationException;
import com.abc.expensevoucher.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final Set<String> SEED_FILES = Set.of(
            "seed-ananya.png",
            "seed-karan.png",
            "seed-meera.png",
            "seed-director.png",
            "seed-accounts.png"
    );

    private final Path signatureDirectory;
    private final long maxBytes;

    public FileStorageService(AppProperties properties) {
        this.signatureDirectory = Path.of(properties.getUploadDir(), "signatures").toAbsolutePath().normalize();
        this.maxBytes = properties.getMaxSignatureBytes();
    }

    public String store(MultipartFile file) {
        byte[] bytes = readBytes(file);
        validate(file, bytes);
        String extension = extensionFor(bytes, file.getOriginalFilename());
        String storedName = UUID.randomUUID() + extension;
        write(storedName, bytes);
        log.info("Stored signature file {}", storedName);
        return storedName;
    }

    public String copy(String existingFileName) {
        Path source = resolve(existingFileName);
        if (!Files.exists(source)) {
            throw new FileValidationException("Saved signature was not found");
        }
        String name = source.getFileName().toString().toLowerCase(Locale.ROOT);
        String extension = name.endsWith(".png") ? ".png" : ".jpg";
        String storedName = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(signatureDirectory);
            Files.copy(source, signatureDirectory.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileValidationException("Could not copy the saved signature");
        }
        return storedName;
    }

    public Path resolve(String fileName) {
        String safe = sanitize(fileName);
        Path resolved = signatureDirectory.resolve(safe).normalize();
        if (!resolved.startsWith(signatureDirectory)) {
            throw new ResourceNotFoundException("File not found");
        }
        return resolved;
    }

    public void ensureSeedSignatures() {
        try {
            Files.createDirectories(signatureDirectory);
        } catch (IOException ex) {
            log.warn("Could not create signature directory {}", signatureDirectory, ex);
            return;
        }
        for (String name : SEED_FILES) {
            Path target = signatureDirectory.resolve(name);
            if (Files.exists(target)) {
                continue;
            }
            ClassPathResource resource = new ClassPathResource("seed-signatures/" + name);
            try (InputStream input = resource.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
                log.info("Copied seed signature {}", name);
            } catch (IOException ex) {
                log.warn("Could not copy seed signature {}", name, ex);
            }
        }
    }

    public String mediaType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        return "image/jpeg";
    }

    private void write(String storedName, byte[] bytes) {
        try {
            Files.createDirectories(signatureDirectory);
            Files.write(signatureDirectory.resolve(storedName), bytes);
        } catch (IOException ex) {
            throw new FileValidationException("Could not store the signature file");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("Signature file is required");
        }
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new FileValidationException("Could not read the signature file");
        }
    }

    private void validate(MultipartFile file, byte[] bytes) {
        if (bytes.length > maxBytes) {
            throw new FileValidationException("Signature file must be 2 MB or smaller");
        }
        if (!isPng(bytes) && !isJpeg(bytes)) {
            throw new FileValidationException("Signature must be a PNG or JPG image");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!original.isBlank() && !original.endsWith(".png") && !original.endsWith(".jpg") && !original.endsWith(".jpeg")) {
            throw new FileValidationException("Signature must be a PNG or JPG image");
        }
    }

    private String extensionFor(byte[] bytes, String originalName) {
        if (isPng(bytes)) {
            return ".png";
        }
        return ".jpg";
    }

    private boolean isPng(byte[] bytes) {
        return bytes.length >= 4
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47;
    }

    private boolean isJpeg(byte[] bytes) {
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private String sanitize(String fileName) {
        if (fileName == null || !fileName.matches("[A-Za-z0-9._-]+") || fileName.contains("..")) {
            throw new ResourceNotFoundException("File not found");
        }
        return fileName;
    }
}
