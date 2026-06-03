package com.artgallery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comments_illustration_created", columnList = "illustration_id, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "illustration_id", nullable = false)
    @JsonIgnore
    private Illustration illustration;

    @JsonProperty("illustrationId")
    public String getIllustrationIdString() {
        return illustration != null ? illustration.getId().toString() : null;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonProperty("userId")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_comment_id")
    @JsonIgnore
    private Comment parentComment;

    @JsonProperty("parentCommentId")
    public String getParentCommentIdString() {
        return parentComment != null ? parentComment.getId().toString() : null;
    }

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
}
