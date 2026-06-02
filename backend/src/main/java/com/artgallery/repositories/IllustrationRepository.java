package com.artgallery.repositories;

import com.artgallery.domain.Illustration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface IllustrationRepository extends JpaRepository<Illustration, UUID> {
    
    // Find illustrations by artist with various visibility rules
    List<Illustration> findByArtistIdOrderByCreatedAtDesc(UUID artistId);
    
    // Dynamic query helper for feeds
    @Query("SELECT DISTINCT i FROM Illustration i LEFT JOIN i.tags t " +
           "WHERE (:visibility IS NULL OR i.visibility IN :visibility) " +
           "AND (:artistId IS NULL OR i.artist.id = :artistId) " +
           "AND (:tag IS NULL OR LOWER(t) = LOWER(:tag)) " +
           "AND (:search IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:startDate IS NULL OR i.createdAt >= :startDate)")
    List<Illustration> findFeedsWithFilters(
            @Param("visibility") List<String> visibility,
            @Param("artistId") UUID artistId,
            @Param("tag") String tag,
            @Param("search") String search,
            @Param("startDate") Date startDate,
            Pageable pageable
    );

    // Feed of followed artists
    @Query("SELECT i FROM Illustration i WHERE i.artist.id IN :followedArtistIds AND i.visibility <> 'private' ORDER BY i.createdAt DESC")
    List<Illustration> findFollowedFeed(@Param("followedArtistIds") List<UUID> followedArtistIds);

    // Bookmarked illustrations for a user
    @Query("SELECT i FROM Illustration i JOIN Bookmark b ON b.illustration.id = i.id WHERE b.user.id = :userId AND i.visibility IN ('everyone', 'logged_in') ORDER BY b.createdAt DESC")
    List<Illustration> findBookmarkedIllustrations(@Param("userId") UUID userId);

    // Top trending tags (aggregating frequency of tag occurrence)
    @Query("SELECT LOWER(t) as tagName, COUNT(i) as tagCount FROM Illustration i JOIN i.tags t GROUP BY LOWER(t) ORDER BY COUNT(i) DESC")
    List<Object[]> getTrendingTags(Pageable pageable);

    // Autosuggest/Search tags matching query string
    @Query("SELECT DISTINCT t as tagName, COUNT(i) as tagCount FROM Illustration i JOIN i.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :search, '%')) GROUP BY t ORDER BY COUNT(i) DESC")
    List<Object[]> searchTags(@Param("search") String search, Pageable pageable);

    // Counts illustrations by artist
    long countByArtistId(UUID artistId);
}
