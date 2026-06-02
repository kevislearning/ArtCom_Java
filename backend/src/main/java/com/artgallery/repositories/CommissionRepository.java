package com.artgallery.repositories;

import com.artgallery.domain.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CommissionRepository extends JpaRepository<Commission, UUID> {
    List<Commission> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    List<Commission> findByArtistIdOrderByCreatedAtDesc(UUID artistId);
    List<Commission> findByClientIdOrArtistIdOrderByCreatedAtDesc(UUID clientId, UUID artistId);
}
