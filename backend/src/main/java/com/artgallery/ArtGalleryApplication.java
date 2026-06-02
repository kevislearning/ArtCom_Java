package com.artgallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ArtGalleryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtGalleryApplication.class, args);
    }
}

