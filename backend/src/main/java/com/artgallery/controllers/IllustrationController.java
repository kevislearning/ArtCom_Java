package com.artgallery.controllers;

import com.artgallery.domain.Illustration;
import com.artgallery.domain.User;
import com.artgallery.domain.Like;
import com.artgallery.domain.Bookmark;
import com.artgallery.domain.Follow;
import com.artgallery.repositories.*;
import com.artgallery.services.CloudinaryService;
import com.artgallery.services.AiDetectionService;
import com.artgallery.services.NotificationService;
import com.artgallery.services.StatsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/illustrations")
public class IllustrationController {

    @Autowired
    private IllustrationRepository illustrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private AiDetectionService aiDetectionService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StatsService statsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Ánh xạ entity Illustration để thêm các thuộc tính liked/bookmarked tương ứng với người dùng
    private Map<String, Object> mapToResponse(Illustration ill, User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("_id", ill.getId().toString());
        map.put("title", ill.getTitle());
        map.put("description", ill.getDescription());
        map.put("imageUrls", ill.getImageUrls());
        map.put("tags", ill.getTags());
        map.put("visibility", ill.getVisibility());
        map.put("commentsEnabled", ill.isCommentsEnabled());
        map.put("isAIGenerated", ill.isAIGenerated());
        map.put("aiDetectionResult", ill.getAiDetectionResult());
        map.put("viewsCount", ill.getViewsCount());
        map.put("likesCount", ill.getLikesCount());
        map.put("bookmarksCount", ill.getBookmarksCount());
        map.put("commentsCount", ill.getCommentsCount());
        map.put("createdAt", ill.getCreatedAt());
        map.put("updatedAt", ill.getUpdatedAt());

        // Chi tiết thông tin Họa sĩ (Artist)
        Map<String, Object> artistMap = new HashMap<>();
        artistMap.put("_id", ill.getArtist().getId().toString());
        artistMap.put("username", ill.getArtist().getUsername());
        artistMap.put("nickname", ill.getArtist().getNickname());
        artistMap.put("avatarUrl", ill.getArtist().getAvatarUrl());
        artistMap.put("isArtist", ill.getArtist().isArtist());
        map.put("artistId", artistMap);

        // Các chỉ số tương tác (liked, bookmarked)
        boolean liked = false;
        boolean bookmarked = false;
        if (user != null) {
            liked = likeRepository.existsByUserIdAndIllustrationId(user.getId(), ill.getId());
            bookmarked = bookmarkRepository.existsByUserIdAndIllustrationId(user.getId(), ill.getId());
        }
        map.put("liked", liked);
        map.put("bookmarked", bookmarked);

        return map;
    }

    @GetMapping
    public ResponseEntity<?> getIllustrations(
            @AuthenticationPrincipal User authUser,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "artistId", required = false) String artistIdStr,
            @RequestParam(value = "period", required = false) String period,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {

        List<String> visibilities = new ArrayList<>();
        visibilities.add("everyone");
        if (authUser != null) {
            visibilities.add("logged_in");
        }

        UUID artistId = null;
        if (artistIdStr != null && !artistIdStr.isEmpty()) {
            artistId = UUID.fromString(artistIdStr);
            // Nếu họa sĩ đang truy vấn feed của chính họ, bỏ qua các giới hạn hiển thị tiêu chuẩn
            if (authUser != null && authUser.getId().equals(artistId)) {
                visibilities = null; // giá trị null đại diện cho việc bỏ qua bộ lọc / lấy toàn bộ chế độ hiển thị
            }
        }

        Date startDate = null;
        if (period != null && !period.isEmpty()) {
            Calendar cal = Calendar.getInstance();
            if ("day".equalsIgnoreCase(period)) {
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else if ("week".equalsIgnoreCase(period)) {
                cal.add(Calendar.DAY_OF_YEAR, -7);
            } else if ("month".equalsIgnoreCase(period)) {
                cal.add(Calendar.MONTH, -1);
            } else if ("year".equalsIgnoreCase(period)) {
                cal.add(Calendar.YEAR, -1);
            }
            startDate = cal.getTime();
        }

        List<Illustration> illustrations = illustrationRepository.findFeedsWithFilters(
                visibilities, artistId, tag, search, startDate, PageRequest.of(0, 1000)
        );

        // Sắp xếp các mục một cách động trong Java để khớp chính xác với logic sắp xếp của Node.js
        if (sort != null) {
            if ("popular".equalsIgnoreCase(sort)) {
                // sắp xếp theo likesCount giảm dần, bookmarksCount giảm dần, viewsCount giảm dần
                illustrations.sort((a, b) -> {
                    int c1 = Integer.compare(b.getLikesCount(), a.getLikesCount());
                    if (c1 != 0) return c1;
                    int c2 = Integer.compare(b.getBookmarksCount(), a.getBookmarksCount());
                    if (c2 != 0) return c2;
                    return Integer.compare(b.getViewsCount(), a.getViewsCount());
                });
            } else if ("recommended".equalsIgnoreCase(sort)) {
                // sắp xếp theo viewsCount giảm dần, likesCount giảm dần
                illustrations.sort((a, b) -> {
                    int c1 = Integer.compare(b.getViewsCount(), a.getViewsCount());
                    if (c1 != 0) return c1;
                    return Integer.compare(b.getLikesCount(), a.getLikesCount());
                });
            } else if ("oldest".equalsIgnoreCase(sort)) {
                illustrations.sort(Comparator.comparing(Illustration::getCreatedAt));
            } else if ("popularity".equalsIgnoreCase(sort)) {
                illustrations.sort((a, b) -> Integer.compare(b.getViewsCount(), a.getViewsCount()));
            } else if ("likes".equalsIgnoreCase(sort)) {
                illustrations.sort((a, b) -> Integer.compare(b.getLikesCount(), a.getLikesCount()));
            } else if ("bookmarks".equalsIgnoreCase(sort)) {
                illustrations.sort((a, b) -> Integer.compare(b.getBookmarksCount(), a.getBookmarksCount()));
            } else {
                illustrations.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            }
        } else {
            // mặc định là mới nhất
            illustrations.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }

        List<Map<String, Object>> mapped = illustrations.stream()
                .map(i -> mapToResponse(i, authUser))
                .collect(Collectors.toList());

        if (page != null && limit != null && limit > 0) {
            int start = (page - 1) * limit;
            if (start >= mapped.size()) {
                mapped = Collections.emptyList();
            } else {
                int end = Math.min(start + limit, mapped.size());
                mapped = mapped.subList(start, end);
            }
        }

        return ResponseEntity.ok(mapped);
    }

    @PostMapping
    public ResponseEntity<?> createIllustration(
            @AuthenticationPrincipal User authUser,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tagsStr,
            @RequestParam(value = "visibility", required = false) String visibility,
            @RequestParam(value = "commentsEnabled", required = false) String commentsEnabledStr,
            @RequestParam(value = "isAIGenerated", required = false) String isAIGeneratedStr,
            @RequestParam("images") List<MultipartFile> files) throws IOException {

        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "At least one image file is required"));
        }

        List<String> tags = new ArrayList<>();
        if (tagsStr != null && !tagsStr.trim().isEmpty()) {
            try {
                String[] parsed = objectMapper.readValue(tagsStr, String[].class);
                tags.addAll(Arrays.asList(parsed));
            } catch (Exception e) {
                String[] split = tagsStr.split(",");
                for (String t : split) {
                    if (!t.trim().isEmpty()) tags.add(t.trim());
                }
            }
        }

        boolean userDeclaredAI = "true".equalsIgnoreCase(isAIGeneratedStr);
        boolean isAIDetected = false;
        double aiProbability = 0.0;

        // Thực hiện quét AI bằng Hugging Face nếu người dùng không khai báo tự động
        if (!userDeclaredAI && !files.isEmpty()) {
            System.out.println("[IllustrationController] Performing AI detection on upload: " + title);
            AiDetectionService.DetectionResult scan = aiDetectionService.scanImage(files.get(0));
            isAIDetected = scan.isAIDetected;
            aiProbability = scan.aiProbability;
        }

        // Upload ảnh lên Cloudinary hoặc dùng bộ nhớ lưu trữ cục bộ làm dự phòng
        List<String> imageUrls = cloudinaryService.uploadMultipleFiles(files);

        // Lấy đối tượng người dùng (User) đã được tải lại từ database
        User artist = userRepository.findById(authUser.getId()).orElseThrow();

        Illustration illustration = Illustration.builder()
                .artist(artist)
                .title(title)
                .description(description != null ? description : "")
                .imageUrls(imageUrls)
                .tags(tags)
                .visibility(visibility != null ? visibility : "everyone")
                .commentsEnabled(!"false".equalsIgnoreCase(commentsEnabledStr))
                .isAIGenerated(userDeclaredAI)
                .aiDetectionResult(new Illustration.AIDetectionResult(isAIDetected, aiProbability))
                .viewsCount(0)
                .likesCount(0)
                .bookmarksCount(0)
                .commentsCount(0)
                .build();

        illustration = illustrationRepository.save(illustration);

        // Gửi thông báo tới tất cả người theo dõi của họa sĩ
        List<Follow> followers = followRepository.findByFollowingId(artist.getId());
        for (Follow f : followers) {
            notificationService.createNotification(
                    f.getFollower().getId(),
                    artist.getId(),
                    "new_illustration",
                    illustration.getId(),
                    "Illustration",
                    artist.getNickname() + " posted a new work: \"" + title + "\""
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(illustration);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIllustrationById(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id) {
        
        Optional<Illustration> illOpt = illustrationRepository.findById(id);
        if (illOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Illustration not found"));
        }

        Illustration ill = illOpt.get();

        // Tăng số lượt xem (viewsCount)
        ill.setViewsCount(ill.getViewsCount() + 1);
        illustrationRepository.save(ill);

        // Kích hoạt tính toán lại số liệu thống kê một cách bất đồng bộ
        statsService.updateArtistStats(ill.getArtist().getId());

        return ResponseEntity.ok(mapToResponse(ill, authUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateIllustration(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id,
            @RequestBody Map<String, Object> body) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Illustration> illOpt = illustrationRepository.findById(id);
        if (illOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Illustration not found"));
        }

        Illustration ill = illOpt.get();
        if (!ill.getArtist().getId().equals(authUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Not authorized to edit this illustration"));
        }

        String title = (String) body.get("title");
        String description = (String) body.get("description");
        String visibility = (String) body.get("visibility");
        Boolean commentsEnabled = (Boolean) body.get("commentsEnabled");
        Object tagsObj = body.get("tags");

        if (title != null) ill.setTitle(title);
        if (description != null) ill.setDescription(description);
        if (visibility != null) ill.setVisibility(visibility);
        if (commentsEnabled != null) ill.setCommentsEnabled(commentsEnabled);

        if (tagsObj != null) {
            List<String> tags = new ArrayList<>();
            if (tagsObj instanceof List) {
                for (Object o : (List<?>) tagsObj) {
                    tags.add(o.toString());
                }
            } else if (tagsObj instanceof String) {
                String[] split = ((String) tagsObj).split(",");
                for (String t : split) {
                    if (!t.trim().isEmpty()) tags.add(t.trim());
                }
            }
            ill.setTags(tags);
        }

        ill = illustrationRepository.save(ill);
        return ResponseEntity.ok(ill);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIllustration(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Illustration> illOpt = illustrationRepository.findById(id);
        if (illOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Illustration not found"));
        }

        Illustration ill = illOpt.get();
        if (!ill.getArtist().getId().equals(authUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You are not authorized to delete this work"));
        }

        // Giữ một bản sao danh sách URL ảnh để thực hiện xóa khỏi bộ lưu trữ
        List<String> imageUrlsToDelete = new ArrayList<>(ill.getImageUrls());

        illustrationRepository.delete(ill);

        // Xóa các lượt thích (Like) và đánh dấu (Bookmark) liên kết
        likeRepository.deleteByUserIdAndIllustrationId(authUser.getId(), ill.getId());
        bookmarkRepository.deleteByUserIdAndIllustrationId(authUser.getId(), ill.getId());

        // Cập nhật số liệu thống kê
        statsService.updateArtistStats(authUser.getId());

        // Xóa các file ảnh khỏi bộ lưu trữ
        if (imageUrlsToDelete != null) {
            for (String url : imageUrlsToDelete) {
                try {
                    cloudinaryService.deleteFile(url);
                } catch (Exception e) {
                    System.err.println("Failed to delete image file: " + url + " - " + e.getMessage());
                }
            }
        }

        return ResponseEntity.ok(Map.of("message", "Illustration deleted successfully"));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Illustration> illOpt = illustrationRepository.findById(id);
        if (illOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Illustration not found"));
        }

        Illustration ill = illOpt.get();
        Optional<Like> likeOpt = likeRepository.findByUserIdAndIllustrationId(authUser.getId(), id);

        boolean liked;
        if (likeOpt.isPresent()) {
            likeRepository.delete(likeOpt.get());
            ill.setLikesCount(Math.max(0, ill.getLikesCount() - 1));
            liked = false;
        } else {
            User user = userRepository.findById(authUser.getId()).orElseThrow();
            Like like = Like.builder().user(user).illustration(ill).build();
            likeRepository.save(like);
            ill.setLikesCount(ill.getLikesCount() + 1);
            liked = true;

            // Kích hoạt gửi thông báo
            notificationService.createNotification(
                    ill.getArtist().getId(),
                    user.getId(),
                    "like",
                    ill.getId(),
                    "Illustration",
                    user.getNickname() + " liked your work: \"" + ill.getTitle() + "\""
            );
        }

        ill = illustrationRepository.save(ill);

        // Cập nhật số liệu thống kê một cách bất đồng bộ
        statsService.updateArtistStats(ill.getArtist().getId());

        return ResponseEntity.ok(Map.of("liked", liked, "likesCount", ill.getLikesCount()));
    }

    @PostMapping("/{id}/bookmark")
    public ResponseEntity<?> toggleBookmark(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Illustration> illOpt = illustrationRepository.findById(id);
        if (illOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Illustration not found"));
        }

        Illustration ill = illOpt.get();
        Optional<Bookmark> bookmarkOpt = bookmarkRepository.findByUserIdAndIllustrationId(authUser.getId(), id);

        boolean bookmarked;
        if (bookmarkOpt.isPresent()) {
            bookmarkRepository.delete(bookmarkOpt.get());
            ill.setBookmarksCount(Math.max(0, ill.getBookmarksCount() - 1));
            bookmarked = false;
        } else {
            User user = userRepository.findById(authUser.getId()).orElseThrow();
            Bookmark bookmark = Bookmark.builder().user(user).illustration(ill).build();
            bookmarkRepository.save(bookmark);
            ill.setBookmarksCount(ill.getBookmarksCount() + 1);
            bookmarked = true;

            // Kích hoạt gửi thông báo
            notificationService.createNotification(
                    ill.getArtist().getId(),
                    user.getId(),
                    "bookmark",
                    ill.getId(),
                    "Illustration",
                    user.getNickname() + " bookmarked your work: \"" + ill.getTitle() + "\""
            );
        }

        ill = illustrationRepository.save(ill);

        // Cập nhật số liệu thống kê một cách bất đồng bộ
        statsService.updateArtistStats(ill.getArtist().getId());

        return ResponseEntity.ok(Map.of("bookmarked", bookmarked, "bookmarksCount", ill.getBookmarksCount()));
    }

    @GetMapping("/followed")
    public ResponseEntity<?> getFollowedFeed(@AuthenticationPrincipal User authUser) {
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        List<Follow> following = followRepository.findByFollowerId(authUser.getId());
        List<UUID> followingIds = following.stream()
                .map(f -> f.getFollowing().getId())
                .collect(Collectors.toList());

        if (followingIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<Illustration> illustrations = illustrationRepository.findFollowedFeed(followingIds);
        List<Map<String, Object>> mapped = illustrations.stream()
                .map(i -> mapToResponse(i, authUser))
                .collect(Collectors.toList());

        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/trending-tags")
    public ResponseEntity<?> getTrendingTags() {
        List<Object[]> queryResults = illustrationRepository.getTrendingTags(PageRequest.of(0, 10));
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : queryResults) {
            result.add(Map.of(
                    "_id", row[0], // tên tag viết thường
                    "count", row[1]
            ));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/feed/bookmarks")
    public ResponseEntity<?> getBookmarkedIllustrations(@AuthenticationPrincipal User authUser) {
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        List<Illustration> bookmarked = illustrationRepository.findBookmarkedIllustrations(authUser.getId());
        List<Map<String, Object>> mapped = bookmarked.stream()
                .map(i -> mapToResponse(i, authUser))
                .collect(Collectors.toList());

        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/tags/search")
    public ResponseEntity<?> searchTags(@RequestParam("search") String search) {
        if (search == null || search.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<Object[]> queryResults = illustrationRepository.searchTags(search.trim(), PageRequest.of(0, 20));
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : queryResults) {
            result.add(Map.of(
                    "_id", row[0],
                    "count", row[1]
            ));
        }
        return ResponseEntity.ok(result);
    }
}
