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
    
    // Tìm các tác phẩm của họa sĩ với các cấu hình chế độ hiển thị khác nhau
    List<Illustration> findByArtistIdOrderByCreatedAtDesc(UUID artistId);
    
    // Bộ lọc truy vấn động (dynamic query) cho bảng tin (feed)
    @Query("SELECT DISTINCT i FROM Illustration i LEFT JOIN i.tags t " +
           "WHERE (:visibility IS NULL OR i.visibility IN :visibility) " +
           "AND (CAST(:artistId AS string) IS NULL OR i.artist.id = :artistId) " +
           "AND (CAST(:tag AS string) IS NULL OR LOWER(t) = LOWER(CAST(:tag AS string))) " +
           "AND (CAST(:search AS string) IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(t) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR i.createdAt >= :startDate)")
    List<Illustration> findFeedsWithFilters(
            @Param("visibility") List<String> visibility,
            @Param("artistId") UUID artistId,
            @Param("tag") String tag,
            @Param("search") String search,
            @Param("startDate") Date startDate,
            Pageable pageable
    );

    // Bảng tin của các họa sĩ đang theo dõi
    @Query("SELECT i FROM Illustration i WHERE i.artist.id IN :followedArtistIds AND i.visibility <> 'private' ORDER BY i.createdAt DESC")
    List<Illustration> findFollowedFeed(@Param("followedArtistIds") List<UUID> followedArtistIds);

    // Các tác phẩm đã đánh dấu (bookmark) của người dùng
    @Query("SELECT i FROM Illustration i JOIN Bookmark b ON b.illustration.id = i.id WHERE b.user.id = :userId AND i.visibility IN ('everyone', 'logged_in') ORDER BY b.createdAt DESC")
    List<Illustration> findBookmarkedIllustrations(@Param("userId") UUID userId);

    // Các tag thịnh hành nhất (tổng hợp theo tần suất xuất hiện)
    @Query("SELECT LOWER(t) as tagName, COUNT(i) as tagCount FROM Illustration i JOIN i.tags t GROUP BY LOWER(t) ORDER BY COUNT(i) DESC")
    List<Object[]> getTrendingTags(Pageable pageable);

    // Gợi ý tự động hoặc tìm kiếm tag khớp với chuỗi tìm kiếm
    @Query("SELECT DISTINCT t as tagName, COUNT(i) as tagCount FROM Illustration i JOIN i.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) GROUP BY t ORDER BY COUNT(i) DESC")
    List<Object[]> searchTags(@Param("search") String search, Pageable pageable);

    // Đếm số lượng tác phẩm của một họa sĩ
    long countByArtistId(UUID artistId);
}
