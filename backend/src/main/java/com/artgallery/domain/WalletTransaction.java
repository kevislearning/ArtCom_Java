package com.artgallery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "wallet_transactions", indexes = {
    @Index(name = "idx_wallet_transactions_user_created", columnList = "user_id, created_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @JsonProperty("_id")
    public String getMongoId() {
        return id != null ? id.toString() : null;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false, length = 30)
    private String type; // deposit, withdraw, escrow_hold, escrow_release, escrow_refund

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reference_id")
    private Commission reference;

    private String description;

    @Column(name = "created_at")
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        if (this.description == null) this.description = "";
    }
}
