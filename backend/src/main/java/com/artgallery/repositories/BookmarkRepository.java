package com.artgallery.repositories;

import com.artgallery.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
    Optional<Bookmark> findByUserIdAndIllustrationId(UUID userId, UUID illustrationId);
    void deleteByUserIdAndIllustrationId(UUID userId, UUID illustrationId);
    boolean existsByUserIdAndIllustrationId(UUID userId, UUID illustrationId);
    long countByIllustrationId(UUID illustrationId);
    List<Bookmark> findByUserId(UUID userId);

    @Query("SELECT b.illustration.id FROM Bookmark b WHERE b.user.id = :userId")
    List<UUID> findBookmarkedIllustrationIdsByUserId(@Param("userId") UUID userId);
}
