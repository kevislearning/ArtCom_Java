package com.artgallery.repositories;

import com.artgallery.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByIllustrationIdOrderByCreatedAtAsc(UUID illustrationId);
    void deleteByIllustrationId(UUID illustrationId);
    List<Comment> findByParentComment(Comment parentComment);
}
