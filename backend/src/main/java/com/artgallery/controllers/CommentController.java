package com.artgallery.controllers;

import com.artgallery.domain.Comment;
import com.artgallery.domain.Illustration;
import com.artgallery.domain.User;
import com.artgallery.repositories.CommentRepository;
import com.artgallery.repositories.IllustrationRepository;
import com.artgallery.repositories.UserRepository;
import com.artgallery.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IllustrationRepository illustrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getComments(@RequestParam("illustrationId") UUID illustrationId) {
        List<Comment> comments = commentRepository.findByIllustrationIdOrderByCreatedAtAsc(illustrationId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<?> createComment(
            @AuthenticationPrincipal User authUser,
            @RequestBody Map<String, Object> body) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        String illustrationIdStr = (String) body.get("illustrationId");
        String content = (String) body.get("content");
        String parentCommentIdStr = (String) body.get("parentCommentId");

        if (illustrationIdStr == null || content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Illustration and content are required"));
        }

        UUID illustrationId = UUID.fromString(illustrationIdStr);
        Optional<Illustration> illOpt = illustrationRepository.findById(illustrationId);
        if (illOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Illustration not found"));
        }

        Illustration ill = illOpt.get();
        User user = userRepository.findById(authUser.getId()).orElseThrow();

        Comment parentComment = null;
        if (parentCommentIdStr != null && !parentCommentIdStr.trim().isEmpty()) {
            parentComment = commentRepository.findById(UUID.fromString(parentCommentIdStr)).orElse(null);
        }

        Comment comment = Comment.builder()
                .illustration(ill)
                .user(user)
                .parentComment(parentComment)
                .content(content.trim())
                .build();

        comment = commentRepository.save(comment);

        // Update counters
        ill.setCommentsCount(ill.getCommentsCount() + 1);
        illustrationRepository.save(ill);

        user.setTotalComments(user.getTotalComments() + 1);
        userRepository.save(user);

        // Notifications
        if (parentComment != null) {
            // Reply: Notify parent comment owner if not me
            if (!parentComment.getUser().getId().equals(user.getId())) {
                notificationService.createNotification(
                        parentComment.getUser().getId(),
                        user.getId(),
                        "reply",
                        ill.getId(),
                        "Illustration",
                        user.getNickname() + " replied to your comment: \"" + content + "\""
                );
            }
        } else {
            // New Direct Comment: Notify artist if not me
            if (!ill.getArtist().getId().equals(user.getId())) {
                notificationService.createNotification(
                        ill.getArtist().getId(),
                        user.getId(),
                        "comment",
                        ill.getId(),
                        "Illustration",
                        user.getNickname() + " commented on your work: \"" + ill.getTitle() + "\""
                );
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}
