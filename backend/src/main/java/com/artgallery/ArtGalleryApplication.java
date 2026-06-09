package com.artgallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import java.io.File;
import java.nio.file.Files;

@SpringBootApplication
@EnableAsync
public class ArtGalleryApplication {

    public static void main(String[] args) {
        // Tải file .env vào thuộc tính hệ thống một cách lập trình
        try {
            File envFile = new File(".env");
            if (!envFile.exists()) {
                envFile = new File("backend/.env");
            }
            if (envFile.exists()) {
                Files.lines(envFile.toPath())
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#") && line.contains("="))
                    .forEach(line -> {
                        int eqIdx = line.indexOf('=');
                        String key = line.substring(0, eqIdx).trim();
                        String val = line.substring(eqIdx + 1).trim();
                        // Loại bỏ dấu nháy kép hoặc nháy đơn nếu có
                        if (val.startsWith("\"") && val.endsWith("\"")) {
                            val = val.substring(1, val.length() - 1);
                        } else if (val.startsWith("'") && val.endsWith("'")) {
                            val = val.substring(1, val.length() - 1);
                        }
                        System.setProperty(key, val);
                    });
                System.out.println("[ArtGalleryApplication] Loaded environment variables from .env file.");
            }
        } catch (Exception e) {
            System.err.println("[ArtGalleryApplication] Failed to load .env file: " + e.getMessage());
        }

        SpringApplication.run(ArtGalleryApplication.class, args);
    }
}

