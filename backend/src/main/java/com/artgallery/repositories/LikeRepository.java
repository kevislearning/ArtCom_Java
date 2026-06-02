package com.artgallery.repositories;

import com.artgallery.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {
    Optional<Like> findByUserIdAndIllustrationId(UUID userId, UUID illustrationId);
    void deleteByUserIdAndIllustrationId(UUID userId, UUID illustrationId);
    boolean existsByUserIdAndIllustrationId(UUID userId, UUID illustrationId);
    long countByIllustrationId(UUID illustrationId);
    List<Like> findByUserId(UUID userId);
    
    @Query("SELECT l.illustration.id FROM Like l WHERE l.user.id = :userId")
    List<UUID> findLikedIllustrationIdsByUserId(@Param("userId") UUID userId);
}
