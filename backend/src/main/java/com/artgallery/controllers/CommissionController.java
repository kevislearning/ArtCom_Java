package com.artgallery.controllers;

import com.artgallery.domain.Commission;
import com.artgallery.domain.Illustration;
import com.artgallery.domain.User;
import com.artgallery.repositories.CommissionRepository;
import com.artgallery.repositories.IllustrationRepository;
import com.artgallery.repositories.UserRepository;
import com.artgallery.services.CloudinaryService;
import com.artgallery.services.NotificationService;
import com.artgallery.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/commissions")
public class CommissionController {

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IllustrationRepository illustrationRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping
    public ResponseEntity<?> createCommission(
            @AuthenticationPrincipal User authUser,
            @RequestBody Map<String, Object> body) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        String artistIdStr = (String) body.get("artistId");
        String title = (String) body.get("title");
        String description = (String) body.get("description");
        Number priceNum = (Number) body.get("price");
        String deadlineStr = (String) body.get("deadline");
        Boolean isPrivateVal = (Boolean) body.get("isPrivate");
        boolean isPrivate = isPrivateVal != null ? isPrivateVal : false;

        if (artistIdStr == null || title == null || description == null || priceNum == null || deadlineStr == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "All commission fields are required"));
        }

        UUID artistId = UUID.fromString(artistIdStr);
        UUID clientId = authUser.getId();

        if (artistId.equals(clientId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "You cannot commission yourself"));
        }

        Optional<User> artistOpt = userRepository.findById(artistId);
        if (artistOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Selected user is not open for commission"));
        }

        User artist = artistOpt.get();
        if (!artist.isArtist()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Selected user is not open for commission"));
        }

        double price = priceNum.doubleValue();

        // Check client balance reload from DB
        User client = userRepository.findById(clientId).orElseThrow();
        if (client.getWalletBalance() < price) {
            return ResponseEntity.badRequest().body(Map.of("message", "Insufficient wallet balance"));
        }

        // Create commission
        Commission commission = Commission.builder()
                .client(client)
                .artist(artist)
                .title(title)
                .description(description)
                .price(price)
                .deadline(Date.from(java.time.Instant.parse(deadlineStr)))
                .isPrivate(isPrivate)
                .status("pending")
                .paymentStatus("unpaid")
                .build();

        commission = commissionRepository.save(commission);

        // Lock client balance in escrow
        walletService.escrowHold(clientId, price, "Đặt cọc cho yêu cầu vẽ commission: \"" + title + "\"", commission);

        // Trigger notification
        notificationService.createNotification(
                artistId,
                clientId,
                "commission_update",
                commission.getId(),
                "Commission",
                "New commission request from " + client.getNickname() + ": \"" + title + "\""
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(commission);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptCommission(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Commission> commOpt = commissionRepository.findById(id);
        if (commOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Commission not found"));
        }

        Commission commission = commOpt.get();
        if (!commission.getArtist().getId().equals(authUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Only the requested artist can accept this commission"));
        }

        if (!"pending".equals(commission.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Commission cannot be accepted at this stage"));
        }

        commission.setStatus("accepted");
        commission = commissionRepository.save(commission);

        // Notify client
        notificationService.createNotification(
                commission.getClient().getId(),
                authUser.getId(),
                "commission_update",
                commission.getId(),
                "Commission",
                "Artist accepted your commission: \"" + commission.getTitle() + "\""
        );

        return ResponseEntity.ok(commission);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectCommission(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Commission> commOpt = commissionRepository.findById(id);
        if (commOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Commission not found"));
        }

        Commission commission = commOpt.get();
        if (!commission.getArtist().getId().equals(authUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Only the requested artist can reject this commission"));
        }

        if (!"pending".equals(commission.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Commission cannot be rejected at this stage"));
        }

        commission.setStatus("rejected");
        commission = commissionRepository.save(commission);

        // Refund client
        walletService.escrowRefund(commission.getClient().getId(), commission.getPrice(), "Hoàn tiền cọc commission do Artist từ chối: \"" + commission.getTitle() + "\"", commission);

        // Notify client
        notificationService.createNotification(
                commission.getClient().getId(),
                authUser.getId(),
                "commission_update",
                commission.getId(),
                "Commission",
                "Artist rejected your commission request: \"" + commission.getTitle() + "\". Funds refunded."
        );

        return ResponseEntity.ok(commission);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelCommission(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Commission> commOpt = commissionRepository.findById(id);
        if (commOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Commission not found"));
        }

        Commission commission = commOpt.get();
        boolean isClient = commission.getClient().getId().equals(authUser.getId());
        boolean isArtist = commission.getArtist().getId().equals(authUser.getId());

        if (!isClient && !isArtist) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Unauthorized"));
        }

        if (isClient && !"pending".equals(commission.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "You cannot cancel a commission after the artist accepts it"));
        }

        if (isArtist && Arrays.asList("completed", "canceled", "rejected").contains(commission.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Commission is already finished"));
        }

        commission.setStatus("canceled");
        commission = commissionRepository.save(commission);

        // Refund client
        walletService.escrowRefund(commission.getClient().getId(), commission.getPrice(), "Hoàn tiền cọc commission do hủy giao dịch: \"" + commission.getTitle() + "\"", commission);

        UUID recipientId = isClient ? commission.getArtist().getId() : commission.getClient().getId();
        notificationService.createNotification(
                recipientId,
                authUser.getId(),
                "commission_update",
                commission.getId(),
                "Commission",
                "Commission \"" + commission.getTitle() + "\" has been canceled. Funds refunded."
        );

        return ResponseEntity.ok(commission);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeCommission(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id,
            @RequestParam("images") List<MultipartFile> files) throws IOException {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Commission> commOpt = commissionRepository.findById(id);
        if (commOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Commission not found"));
        }

        Commission commission = commOpt.get();
        if (!commission.getArtist().getId().equals(authUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Only the commissioned artist can deliver and complete this task"));
        }

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please upload the completed artwork files"));
        }

        commission.setStatus("completed");
        commission.setPaymentStatus("paid_to_artist");

        List<String> imageUrls = cloudinaryService.uploadMultipleFiles(files);

        // Create completed artwork illustration
        Illustration resultIllustration = Illustration.builder()
                .artist(commission.getArtist())
                .title("[Commission Result] " + commission.getTitle())
                .description("Delivery for commission. Original prompt: " + commission.getDescription())
                .imageUrls(imageUrls)
                .tags(Arrays.asList("commission", "delivery"))
                .visibility(commission.isPrivate() ? "private" : "everyone")
                .commentsEnabled(true)
                .viewsCount(0)
                .likesCount(0)
                .bookmarksCount(0)
                .commentsCount(0)
                .build();

        resultIllustration = illustrationRepository.save(resultIllustration);
        commission.setResultIllustration(resultIllustration);

        commission = commissionRepository.save(commission);

        // Release escrow money to artist
        walletService.escrowRelease(commission.getArtist().getId(), commission.getPrice(), "Nhận thanh toán hoàn thành commission: \"" + commission.getTitle() + "\"", commission);

        // Notify client
        notificationService.createNotification(
                commission.getClient().getId(),
                authUser.getId(),
                "commission_update",
                commission.getId(),
                "Commission",
                "Artist completed your commission: \"" + commission.getTitle() + "\". Artwork delivered!"
        );

        return ResponseEntity.ok(commission);
    }

    @GetMapping("/client")
    public ResponseEntity<?> getClientCommissions(@AuthenticationPrincipal User authUser) {
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }
        List<Commission> commissions = commissionRepository.findByClientIdOrderByCreatedAtDesc(authUser.getId());
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/artist")
    public ResponseEntity<?> getArtistCommissions(@AuthenticationPrincipal User authUser) {
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }
        List<Commission> commissions = commissionRepository.findByArtistIdOrderByCreatedAtDesc(authUser.getId());
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCommissionById(
            @AuthenticationPrincipal User authUser,
            @PathVariable("id") UUID id) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Commission> commOpt = commissionRepository.findById(id);
        if (commOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Commission not found"));
        }

        Commission commission = commOpt.get();
        if (!commission.getClient().getId().equals(authUser.getId()) && !commission.getArtist().getId().equals(authUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied to this commission detail"));
        }

        return ResponseEntity.ok(commission);
    }
}
