package com.artgallery.services;

import com.artgallery.domain.Illustration;
import com.artgallery.domain.User;
import com.artgallery.repositories.UserRepository;
import com.artgallery.repositories.IllustrationRepository;
import com.artgallery.repositories.LikeRepository;
import com.artgallery.repositories.BookmarkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class StatsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IllustrationRepository illustrationRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Async
    @Transactional
    public void updateArtistStats(UUID artistId) {
        try {
            User artist = userRepository.findById(artistId).orElse(null);
            if (artist == null) return;

            List<Illustration> illustrations = illustrationRepository.findByArtistIdOrderByCreatedAtDesc(artistId);
            int totalViews = 0;
            long totalLikes = 0;
            long totalBookmarks = 0;

            for (Illustration ill : illustrations) {
                totalViews += ill.getViewsCount();
                totalLikes += likeRepository.countByIllustrationId(ill.getId());
                totalBookmarks += bookmarkRepository.countByIllustrationId(ill.getId());
            }

            artist.setTotalViews(totalViews);
            artist.setTotalLikes((int) totalLikes);
            artist.setTotalBookmarks((int) totalBookmarks);

            userRepository.save(artist);
            System.out.println("[StatsService] Asynchronously updated engagement stats for artist: " + artist.getUsername());
        } catch (Exception e) {
            System.err.println("[StatsService] Error updating artist stats: " + e.getMessage());
        }
    }
}
