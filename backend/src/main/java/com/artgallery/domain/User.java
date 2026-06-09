package com.artgallery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_username", columnList = "username"),
    @Index(name = "idx_users_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    private String nickname;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "is_artist")
    private boolean isArtist;

    @Column(name = "wallet_balance")
    private double walletBalance;

    @Embedded
    private RequestTerms requestTerms;

    @Embedded
    private SocialLinks socialLinks;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "website_value")),
        @AttributeOverride(name = "isPublic", column = @Column(name = "website_is_public"))
    })
    private Website website;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "gender_value")),
        @AttributeOverride(name = "isPublic", column = @Column(name = "gender_is_public"))
    })
    private Gender gender;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "country_value")),
        @AttributeOverride(name = "isPublic", column = @Column(name = "country_is_public"))
    })
    private Country country;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "birthday_value")),
        @AttributeOverride(name = "isPublic", column = @Column(name = "birthday_is_public"))
    })
    private Birthday birthday;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "occupation_value")),
        @AttributeOverride(name = "isPublic", column = @Column(name = "occupation_is_public"))
    })
    private Occupation occupation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_custom_social_links", joinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private List<CustomSocialLink> customSocialLinks = new ArrayList<>();

    @Column(name = "total_views")
    private int totalViews;

    @Column(name = "total_likes")
    private int totalLikes;

    @Column(name = "total_bookmarks")
    private int totalBookmarks;

    @Column(name = "total_comments")
    private int totalComments;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.nickname == null) {
            this.nickname = this.username;
        }
        if (this.bio == null) this.bio = "";
        if (this.avatarUrl == null) this.avatarUrl = "";
        if (this.bannerUrl == null) this.bannerUrl = "";
        if (this.requestTerms == null) {
            this.requestTerms = new RequestTerms("", "", 0.0, "", false);
        }
        if (this.socialLinks == null) {
            this.socialLinks = new SocialLinks("", "", "");
        }
        if (this.website == null) this.website = new Website("", true);
        if (this.gender == null) this.gender = new Gender("other", true);
        if (this.country == null) this.country = new Country("", true);
        if (this.birthday == null) this.birthday = new Birthday(null, true);
        if (this.occupation == null) this.occupation = new Occupation("", true);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }

    // Các Value Object dạng nhúng (Embeddable)
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequestTerms {
        @Column(name = "terms_title")
        private String title;
        
        @Column(name = "terms_details", columnDefinition = "TEXT")
        private String details;
        
        @Column(name = "terms_target_price")
        private double targetPrice;
        
        @Column(name = "terms_background_url")
        private String backgroundUrl;
        
        @Column(name = "terms_has_terms")
        private boolean hasTerms;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SocialLinks {
        private String twitter;
        private String behance;
        private String artstation;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Website {
        private String value;
        @JsonProperty("isPublic")
        private boolean isPublic;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Gender {
        private String value;
        @JsonProperty("isPublic")
        private boolean isPublic;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Country {
        private String value;
        @JsonProperty("isPublic")
        private boolean isPublic;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Birthday {
        private Date value;
        @JsonProperty("isPublic")
        private boolean isPublic;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Occupation {
        private String value;
        @JsonProperty("isPublic")
        private boolean isPublic;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomSocialLink {
        private String platform;
        private String username;
        @JsonProperty("isPublic")
        private boolean isPublic;
    }
}
