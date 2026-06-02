package com.artgallery.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<?> getHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Server is healthy"
        ));
    }
}
