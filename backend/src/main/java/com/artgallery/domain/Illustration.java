package com.artgallery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "illustrations", indexes = {
    @Index(name = "idx_illustrations_created_at", columnList = "created_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Illustration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artist_id", nullable = false)
    private User artist;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "illustration_image_urls", joinColumns = @JoinColumn(name = "illustration_id"))
    @Column(name = "image_url", length = 1000)
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "illustration_tags", joinColumns = @JoinColumn(name = "illustration_id"), indexes = {
        @Index(name = "idx_illustration_tags_tag", columnList = "tag")
    })
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(length = 20)
    private String visibility; // everyone, private, logged_in

    @Column(name = "comments_enabled")
    private boolean commentsEnabled;

    @Column(name = "is_ai_generated")
    private boolean isAIGenerated;

    @Embedded
    private AIDetectionResult aiDetectionResult;

    @Column(name = "views_count")
    private int viewsCount;

    @Column(name = "likes_count")
    private int likesCount;

    @Column(name = "bookmarks_count")
    private int bookmarksCount;

    @Column(name = "comments_count")
    private int commentsCount;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.visibility == null) this.visibility = "everyone";
        if (this.description == null) this.description = "";
        if (this.aiDetectionResult == null) {
            this.aiDetectionResult = new AIDetectionResult(false, 0.0);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AIDetectionResult {
        @Column(name = "is_ai_detected")
        private boolean isAIDetected;

        @Column(name = "ai_probability")
        private double aiProbability;
    }
}
