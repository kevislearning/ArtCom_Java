package com.artgallery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "commissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artist_id", nullable = false)
    private User artist;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private Date deadline;

    @Column(name = "payment_status", length = 30)
    private String paymentStatus; // unpaid, escrow, paid_to_artist, refunded

    @Column(length = 30)
    private String status; // pending, accepted, in_progress, completed, canceled, rejected

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "result_illustration_id")
    private Illustration resultIllustration;

    @Column(name = "is_private")
    private boolean isPrivate;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.paymentStatus == null) this.paymentStatus = "unpaid";
        if (this.status == null) this.status = "pending";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
}
