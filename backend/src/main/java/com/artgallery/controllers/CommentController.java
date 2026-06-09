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
import org.springframework.transaction.annotation.Transactional;
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

    @GetMapping("/illustration/{illustrationId}")
    public ResponseEntity<?> getComments(@PathVariable("illustrationId") UUID illustrationId) {
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

        // Cập nhật các bộ đếm
        ill.setCommentsCount(ill.getCommentsCount() + 1);
        illustrationRepository.save(ill);

        user.setTotalComments(user.getTotalComments() + 1);
        userRepository.save(user);

        // Các thông báo
        if (parentComment != null) {
            // Phản hồi: Thông báo cho người sở hữu bình luận cha nếu không phải chính tôi
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
            // Bình luận trực tiếp mới: Thông báo cho họa sĩ nếu không phải chính tôi
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

    @DeleteMapping("/{commentId}")
    @Transactional
    public ResponseEntity<?> deleteComment(
            @AuthenticationPrincipal User authUser,
            @PathVariable("commentId") UUID commentId) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Comment not found"));
        }

        Comment comment = commentOpt.get();
        
        // Chỉ tác giả bình luận hoặc chủ sở hữu tác phẩm (họa sĩ) mới có quyền xóa
        boolean isCommentOwner = comment.getUser().getId().equals(authUser.getId());
        boolean isIllustrationOwner = comment.getIllustration().getArtist().getId().equals(authUser.getId());
        
        if (!isCommentOwner && !isIllustrationOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You are not authorized to delete this comment"));
        }

        // Giảm bộ đếm của tác giả bình luận chính
        User commentAuthor = comment.getUser();
        commentAuthor.setTotalComments(Math.max(0, commentAuthor.getTotalComments() - 1));
        userRepository.save(commentAuthor);

        // Tìm và xóa các phản hồi
        List<Comment> replies = commentRepository.findByParentComment(comment);
        for (Comment reply : replies) {
            User replyAuthor = reply.getUser();
            replyAuthor.setTotalComments(Math.max(0, replyAuthor.getTotalComments() - 1));
            userRepository.save(replyAuthor);
            commentRepository.delete(reply);
        }

        // Giảm số lượng bình luận của tác phẩm đi 1 + số lượng phản hồi
        Illustration ill = comment.getIllustration();
        int deletedCount = 1 + replies.size();
        ill.setCommentsCount(Math.max(0, ill.getCommentsCount() - deletedCount));
        illustrationRepository.save(ill);

        // Xóa bình luận chính
        commentRepository.delete(comment);

        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
    }
}
