package com.artgallery.controllers;

import com.artgallery.domain.Notification;
import com.artgallery.domain.User;
import com.artgallery.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal User authUser) {
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(authUser.getId());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/mark-all")
    @Transactional
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal User authUser) {
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }
        notificationRepository.markAllAsRead(authUser.getId());
        return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read"));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Notification> notifOpt = notificationRepository.findById(id);
        if (notifOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Notification not found"));
        }

        Notification notification = notifOpt.get();
        if (!notification.getRecipient().getId().equals(authUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied"));
        }

        notification.setRead(true);
        notification = notificationRepository.save(notification);

        return ResponseEntity.ok(notification);
    }
}
