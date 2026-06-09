package com.artgallery.controllers;

import com.artgallery.domain.Follow;
import com.artgallery.domain.User;
import com.artgallery.repositories.FollowRepository;
import com.artgallery.repositories.UserRepository;
import com.artgallery.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggleFollow(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID targetUserId) {

        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        UUID followerId = authUser.getId();
        if (targetUserId.equals(followerId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "You cannot follow yourself"));
        }

        Optional<User> targetOpt = userRepository.findById(targetUserId);
        if (targetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        User targetUser = targetOpt.get();
        Optional<Follow> followOpt = followRepository.findByFollowerIdAndFollowingId(followerId, targetUserId);

        boolean followed;
        String message;
        if (followOpt.isPresent()) {
            followRepository.delete(followOpt.get());
            followed = false;
            message = "Unfollowed user successfully";
        } else {
            User followerUser = userRepository.findById(followerId).orElseThrow();
            Follow follow = Follow.builder().follower(followerUser).following(targetUser).build();
            followRepository.save(follow);
            followed = true;
            message = "Followed user successfully";

            // Kích hoạt gửi thông báo
            notificationService.createNotification(
                    targetUserId,
                    followerId,
                    "follow",
                    null,
                    null,
                    followerUser.getNickname() + " started following you"
            );
        }

        return ResponseEntity.ok(Map.of("followed", followed, "message", message));
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable("id") UUID userId) {
        List<Follow> follows = followRepository.findByFollowingId(userId);
        List<User> followers = follows.stream()
                .map(Follow::getFollower)
                .collect(Collectors.toList());
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<?> getFollowing(@PathVariable("id") UUID userId) {
        List<Follow> follows = followRepository.findByFollowerId(userId);
        List<User> following = follows.stream()
                .map(Follow::getFollowing)
                .collect(Collectors.toList());
        return ResponseEntity.ok(following);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> checkFollowStatus(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID targetUserId) {
        
        if (authUser == null) {
            return ResponseEntity.ok(Map.of("followed", false));
        }

        boolean followed = followRepository.existsByFollowerIdAndFollowingId(authUser.getId(), targetUserId);
        return ResponseEntity.ok(Map.of("followed", followed));
    }
}
