package com.artgallery.controllers;

import com.artgallery.domain.User;
import com.artgallery.domain.Illustration;
import com.artgallery.repositories.UserRepository;
import com.artgallery.repositories.IllustrationRepository;
import com.artgallery.repositories.FollowRepository;
import com.artgallery.security.JwtTokenProvider;
import com.artgallery.services.CloudinaryService;
import com.artgallery.services.GoogleAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IllustrationRepository illustrationRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private CloudinaryService cloudinaryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Hàm hỗ trợ gửi Token trong Response
    private ResponseEntity<Map<String, Object>> sendTokenResponse(User user, int statusCode, HttpServletResponse response) {
        String token = tokenProvider.generateToken(user.getId());

        // Cài đặt httpOnly Cookie
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 ngày
        cookie.setPath("/");
        response.addCookie(cookie);

        Map<String, Object> body = new HashMap<>();
        body.put("_id", user.getId().toString());
        body.put("username", user.getUsername());
        body.put("email", user.getEmail());
        body.put("nickname", user.getNickname());
        body.put("bio", user.getBio());
        body.put("avatarUrl", user.getAvatarUrl());
        body.put("bannerUrl", user.getBannerUrl());
        body.put("isArtist", user.isArtist());
        body.put("requestTerms", user.getRequestTerms());
        body.put("walletBalance", user.getWalletBalance());
        body.put("socialLinks", user.getSocialLinks());
        body.put("website", user.getWebsite());
        body.put("customSocialLinks", user.getCustomSocialLinks());
        body.put("gender", user.getGender());
        body.put("country", user.getCountry());
        body.put("birthday", user.getBirthday());
        body.put("occupation", user.getOccupation());
        body.put("totalViews", user.getTotalViews());
        body.put("totalLikes", user.getTotalLikes());
        body.put("totalBookmarks", user.getTotalBookmarks());
        body.put("totalComments", user.getTotalComments());
        body.put("token", token);

        return ResponseEntity.status(statusCode).body(body);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, Object> body,
            HttpServletResponse response) {
        
        String username = (String) body.get("username");
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        String nickname = (String) body.get("nickname");
        Boolean isArtistVal = (Boolean) body.get("isArtist");
        boolean isArtist = isArtistVal != null ? isArtistVal : false;

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please provide all required fields"));
        }

        if (userRepository.existsByUsernameIgnoreCase(username) || userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username or email already exists"));
        }

        User user = User.builder()
                .username(username.toLowerCase().trim())
                .email(email.toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(password))
                .nickname(nickname != null ? nickname.trim() : username.trim())
                .isArtist(isArtist)
                .walletBalance(0.0)
                .build();

        user = userRepository.save(user);

        return sendTokenResponse(user, HttpStatus.CREATED.value(), response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> body,
            HttpServletResponse response) {
        
        String emailOrUsername = body.get("emailOrUsername");
        String password = body.get("password");

        if (emailOrUsername == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please provide credentials"));
        }

        Optional<User> userOpt = userRepository.findByUsernameOrEmail(emailOrUsername.trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
        }

        return sendTokenResponse(user, HttpStatus.OK.value(), response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "Successfully logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal User authUser) {
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }
        return ResponseEntity.ok(authUser);
    }

    @GetMapping("/artists/recommended")
    public ResponseEntity<?> getRecommended() {
        List<User> artists = userRepository.findTop6ByIsArtistTrueOrderByTotalLikesDescTotalViewsDesc();
        return ResponseEntity.ok(artists);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User authUser,
            @RequestBody Map<String, String> body) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password and new password are required"));
        }

        // Tải lại từ DB để đảm bảo trạng thái đối tượng chính xác
        User user = userRepository.findById(authUser.getId()).orElseThrow();

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu hiện tại không chính xác"));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User authUser,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "isArtist", required = false) String isArtistStr,
            @RequestParam(value = "socialLinks", required = false) String socialLinksStr,
            @RequestParam(value = "website", required = false) String websiteStr,
            @RequestParam(value = "gender", required = false) String genderStr,
            @RequestParam(value = "country", required = false) String countryStr,
            @RequestParam(value = "birthday", required = false) String birthdayStr,
            @RequestParam(value = "occupation", required = false) String occupationStr,
            @RequestParam(value = "customSocialLinks", required = false) String customSocialLinksStr,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
            @RequestParam(value = "banner", required = false) MultipartFile bannerFile) throws IOException {

        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        User user = userRepository.findById(authUser.getId()).orElseThrow();

        if (nickname != null) user.setNickname(nickname);
        if (bio != null) user.setBio(bio);
        if (isArtistStr != null) {
            user.setArtist("true".equalsIgnoreCase(isArtistStr));
        }

        // Hàm hỗ trợ để parse chuỗi JSON của các thuộc tính lồng nhau
        if (socialLinksStr != null) {
            user.setSocialLinks(objectMapper.readValue(socialLinksStr, User.SocialLinks.class));
        }
        if (websiteStr != null) {
            user.setWebsite(objectMapper.readValue(websiteStr, User.Website.class));
        }
        if (genderStr != null) {
            user.setGender(objectMapper.readValue(genderStr, User.Gender.class));
        }
        if (countryStr != null) {
            user.setCountry(objectMapper.readValue(countryStr, User.Country.class));
        }
        if (birthdayStr != null) {
            user.setBirthday(objectMapper.readValue(birthdayStr, User.Birthday.class));
        }
        if (occupationStr != null) {
            user.setOccupation(objectMapper.readValue(occupationStr, User.Occupation.class));
        }
        if (customSocialLinksStr != null) {
            User.CustomSocialLink[] customLinks = objectMapper.readValue(customSocialLinksStr, User.CustomSocialLink[].class);
            user.setCustomSocialLinks(new ArrayList<>(Arrays.asList(customLinks)));
        }

        // Upload các tệp tin
        if (avatarFile != null && !avatarFile.isEmpty()) {
            user.setAvatarUrl(cloudinaryService.uploadFile(avatarFile));
        }
        if (bannerFile != null && !bannerFile.isEmpty()) {
            user.setBannerUrl(cloudinaryService.uploadFile(bannerFile));
        }

        user = userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/request-terms")
    public ResponseEntity<?> updateRequestTerms(
            @AuthenticationPrincipal User authUser,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "details", required = false) String details,
            @RequestParam(value = "targetPrice", required = false) Double targetPrice,
            @RequestParam(value = "background", required = false) MultipartFile file) throws IOException {

        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        User user = userRepository.findById(authUser.getId()).orElseThrow();

        User.RequestTerms terms = user.getRequestTerms();
        if (terms == null) {
            terms = new User.RequestTerms("", "", 0.0, "", true);
        }

        if (title != null) terms.setTitle(title);
        if (details != null) terms.setDetails(details);
        if (targetPrice != null) terms.setTargetPrice(targetPrice);
        terms.setHasTerms(true);

        if (file != null && !file.isEmpty()) {
            terms.setBackgroundUrl(cloudinaryService.uploadFile(file));
        }

        user.setRequestTerms(terms);
        user.setArtist(true); // Đồng bộ hóa các vai trò cho khớp

        user = userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/request-terms")
    public ResponseEntity<?> deleteRequestTerms(@AuthenticationPrincipal User authUser) {
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        User user = userRepository.findById(authUser.getId()).orElseThrow();

        user.setRequestTerms(new User.RequestTerms("", "", 0.0, "", false));
        user.setArtist(false); // Đặt lại các vai trò cho khớp

        user = userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @AuthenticationPrincipal User authUser,
            @RequestParam("search") String search) {
        
        if (search == null || search.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<User> users = userRepository.searchUsers(search.trim());
        List<Map<String, Object>> result = new ArrayList<>();

        for (User u : users) {
            Map<String, Object> uMap = new HashMap<>();
            uMap.put("_id", u.getId().toString());
            uMap.put("username", u.getUsername());
            uMap.put("email", u.getEmail());
            uMap.put("nickname", u.getNickname());
            uMap.put("bio", u.getBio());
            uMap.put("avatarUrl", u.getAvatarUrl());
            uMap.put("bannerUrl", u.getBannerUrl());
            uMap.put("isArtist", u.isArtist());
            uMap.put("walletBalance", u.getWalletBalance());
            uMap.put("totalViews", u.getTotalViews());
            uMap.put("totalLikes", u.getTotalLikes());
            uMap.put("totalBookmarks", u.getTotalBookmarks());
            uMap.put("totalComments", u.getTotalComments());

            // Lấy 3 tác phẩm mới nhất cho mỗi người dùng
            List<Illustration> artworks = illustrationRepository.findByArtistIdOrderByCreatedAtDesc(u.getId());
            List<Map<String, Object>> artList = new ArrayList<>();
            int limit = Math.min(artworks.size(), 3);
            for (int i = 0; i < limit; i++) {
                Illustration art = artworks.get(i);
                if ("everyone".equals(art.getVisibility())) {
                    artList.add(Map.of(
                        "_id", art.getId().toString(),
                        "imageUrls", art.getImageUrls(),
                        "title", art.getTitle()
                    ));
                }
            }
            uMap.put("artworks", artList);

            boolean followed = false;
            if (authUser != null) {
                followed = followRepository.existsByFollowerIdAndFollowingId(authUser.getId(), u.getId());
            }
            uMap.put("followed", followed);

            result.add(uMap);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(
            @RequestBody Map<String, String> body,
            HttpServletResponse response) {
        
        String credential = body.get("credential");
        if (credential == null || credential.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token Google không hợp lệ"));
        }

        GoogleAuthService.GoogleUserInfo googleInfo = googleAuthService.verifyToken(credential);
        if (googleInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Không thể xác thực tài khoản Google"));
        }

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(googleInfo.email);
        User user;

        if (userOpt.isEmpty()) {
            // Thiết lập thông tin cho người dùng mới
            String emailPrefix = googleInfo.email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
            String baseUsername = emailPrefix.isEmpty() ? "user" : emailPrefix;
            String username = baseUsername;

            int counter = 1;
            while (userRepository.existsByUsernameIgnoreCase(username)) {
                username = baseUsername + counter;
                counter++;
            }

            // Tạo mã băm mật khẩu giả lập bảo mật
            String randomPass = UUID.randomUUID().toString();
            user = User.builder()
                    .username(username)
                    .email(googleInfo.email.toLowerCase())
                    .passwordHash(passwordEncoder.encode(randomPass))
                    .nickname(googleInfo.name != null ? googleInfo.name : username)
                    .avatarUrl(googleInfo.pictureUrl != null ? googleInfo.pictureUrl : "")
                    .isArtist(false)
                    .walletBalance(0.0)
                    .build();

            user = userRepository.save(user);
        } else {
            user = userOpt.get();
        }

        return sendTokenResponse(user, HttpStatus.OK.value(), response);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getPublicProfile(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id) {
        
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User profile not found"));
        }

        User user = userOpt.get();
        boolean isMe = authUser != null && authUser.getId().equals(id);

        if (!isMe) {
            // Đặt giá trị null cho các trường riêng tư theo logic ban đầu
            if (user.getWebsite() != null && !user.getWebsite().isPublic()) {
                user.setWebsite(new User.Website("", false));
            }
            if (user.getGender() != null && !user.getGender().isPublic()) {
                user.setGender(new User.Gender("other", false));
            }
            if (user.getCountry() != null && !user.getCountry().isPublic()) {
                user.setCountry(new User.Country("", false));
            }
            if (user.getBirthday() != null && !user.getBirthday().isPublic()) {
                user.setBirthday(new User.Birthday(null, false));
            }
            if (user.getOccupation() != null && !user.getOccupation().isPublic()) {
                user.setOccupation(new User.Occupation("", false));
            }
            if (user.getCustomSocialLinks() != null) {
                List<User.CustomSocialLink> filtered = new ArrayList<>();
                for (User.CustomSocialLink link : user.getCustomSocialLinks()) {
                    if (link.isPublic()) filtered.add(link);
                }
                user.setCustomSocialLinks(filtered);
            }
        }

        return ResponseEntity.ok(user);
    }
}
