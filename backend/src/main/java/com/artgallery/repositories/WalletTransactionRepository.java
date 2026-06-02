package com.artgallery.repositories;

import com.artgallery.domain.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.description LIKE CONCAT('%', :pattern, '%')")
    Optional<WalletTransaction> findByDescriptionPattern(@Param("pattern") String pattern);
}
