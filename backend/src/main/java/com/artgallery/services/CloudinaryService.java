package com.artgallery.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class CloudinaryService {

    private Cloudinary cloudinary;
    private final String localUploadPath = "uploads";

    @Value("${client.url}")
    private String clientUrl;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        
        // Only initialize Cloudinary if credentials are configured and not placeholders
        if (cloudName != null && !cloudName.contains("cloud-name") && !cloudName.isEmpty() &&
            apiKey != null && !apiKey.contains("api-key") && !apiKey.isEmpty() &&
            apiSecret != null && !apiSecret.contains("api-secret") && !apiSecret.isEmpty()) {
            
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            this.cloudinary = new Cloudinary(config);
            System.out.println("[CloudinaryService] Initialized with remote credentials.");
        } else {
            System.out.println("[CloudinaryService] Missing or placeholder credentials. Falling back to local storage.");
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "";
        }

        // If Cloudinary is available, upload to cloud
        if (this.cloudinary != null) {
            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                return uploadResult.get("secure_url").toString();
            } catch (Exception e) {
                System.err.println("[CloudinaryService] Cloud upload failed, trying local fallback: " + e.getMessage());
            }
        }

        // Otherwise fallback to local uploads directory
        return uploadFileLocally(file);
    }

    public List<String> uploadMultipleFiles(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return urls;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                urls.add(uploadFile(file));
            }
        }
        return urls;
    }

    private String uploadFileLocally(MultipartFile file) throws IOException {
        // Ensure directory exists
        Path uploadDirectory = Paths.get(localUploadPath);
        if (!Files.exists(uploadDirectory)) {
            Files.createDirectories(uploadDirectory);
        }

        // Generate unique name
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        Path targetLocation = uploadDirectory.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Return path relative to server URL: http://localhost:5000/uploads/filename
        return "http://localhost:5000/uploads/" + uniqueFilename;
    }
}
