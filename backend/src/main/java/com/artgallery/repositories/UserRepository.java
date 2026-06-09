package com.artgallery.repositories;

import com.artgallery.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:login) OR LOWER(u.email) = LOWER(:login)")
    Optional<User> findByUsernameOrEmail(@Param("login") String login);
    
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);

    // Danh sách 6 họa sĩ hàng đầu sắp xếp giảm dần theo lượt thích (totalLikes) và lượt xem (totalViews)
    List<User> findTop6ByIsArtistTrueOrderByTotalLikesDescTotalViewsDesc();

    // Tìm kiếm người dùng tùy chỉnh theo tên tài khoản (username) hoặc biệt danh (nickname)
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);
}
