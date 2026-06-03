package com.artgallery.controllers;

import com.artgallery.domain.User;
import com.artgallery.domain.WalletTransaction;
import com.artgallery.repositories.UserRepository;
import com.artgallery.repositories.WalletTransactionRepository;
import com.artgallery.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${momo.partner-code}")
    private String momoPartnerCode;

    @Value("${momo.access-key}")
    private String momoAccessKey;

    @Value("${momo.secret-key}")
    private String momoSecretKey;

    @Value("${momo.api-url}")
    private String momoApiUrl;

    @Value("${client.url}")
    private String clientUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // HmacSHA256 signature generator helper
    private String generateMomoSignature(String rawData, String secretKey) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKeySpec);
            byte[] bytes = sha256HMAC.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate MoMo signature: " + e.getMessage(), e);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> depositFunds(
            @AuthenticationPrincipal User authUser,
            @RequestBody Map<String, Object> body) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Number amountNum = (Number) body.get("amount");
        if (amountNum == null || amountNum.doubleValue() < 100000) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số tiền nạp tối thiểu là 100,000 VND!"));
        }

        WalletService.WalletResult result = walletService.deposit(
                authUser.getId(), amountNum.doubleValue(), "Simulated Deposit (Nạp tiền giả lập)"
        );

        return ResponseEntity.ok(Map.of(
                "walletBalance", result.user.getWalletBalance(),
                "transaction", result.transaction
        ));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawFunds(
            @AuthenticationPrincipal User authUser,
            @RequestBody Map<String, Object> body) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Number amountNum = (Number) body.get("amount");
        if (amountNum == null || amountNum.doubleValue() < 50000) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số tiền nạp tối thiểu là 50,000 VND!"));
        }

        try {
            WalletService.WalletResult result = walletService.withdraw(
                    authUser.getId(), amountNum.doubleValue(), "Simulated Withdrawal (Rút tiền giả lập)"
            );
            return ResponseEntity.ok(Map.of(
                    "walletBalance", result.user.getWalletBalance(),
                    "transaction", result.transaction
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(@AuthenticationPrincipal User authUser) {
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }
        List<WalletTransaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(authUser.getId());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@AuthenticationPrincipal User authUser) {
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }
        User user = userRepository.findById(authUser.getId()).orElseThrow();
        return ResponseEntity.ok(Map.of("walletBalance", user.getWalletBalance()));
    }

    @PostMapping("/deposit/momo")
    public ResponseEntity<?> initiateMomoPayment(
            @AuthenticationPrincipal User authUser,
            @RequestBody Map<String, Object> body) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Number amountNum = (Number) body.get("amount");
        if (amountNum == null || amountNum.doubleValue() < 100000) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số tiền nạp tối thiểu là 100,000 VND!"));
        }

        double amount = amountNum.doubleValue();
        String orderId = "ARTPAY_" + authUser.getId() + "_" + System.currentTimeMillis();
        String requestId = orderId;
        String orderInfo = "Nap tien vi gia lap ArtGallery - user @" + authUser.getUsername();
        String baseClientUrl = clientUrl;
        if (clientUrl != null && clientUrl.contains(",")) {
            baseClientUrl = clientUrl.split(",")[0].trim();
        }
        String redirectUrl = baseClientUrl + "/wallet";
        String ipnUrl = "http://localhost:5000/api/wallet/momo-ipn"; // absolute path callback
        String extraData = "";
        String requestType = "captureWallet";

        // Build raw signature string
        String rawSignature = "accessKey=" + momoAccessKey +
                "&amount=" + (long)amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + momoPartnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = generateMomoSignature(rawSignature, momoSecretKey);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("partnerCode", momoPartnerCode);
        requestBody.put("partnerName", "Art Gallery");
        requestBody.put("storeId", "ArtGalleryStore");
        requestBody.put("requestId", requestId);
        requestBody.put("amount", (long)amount);
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", redirectUrl);
        requestBody.put("ipnUrl", ipnUrl);
        requestBody.put("lang", "vi");
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", requestType);
        requestBody.put("signature", signature);

        System.out.println("[MoMo] Requesting payment link for amount: " + amount);

        try {
            ResponseEntity<?> response = restTemplate.postForEntity(momoApiUrl, requestBody, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resBody = (Map<String, Object>) response.getBody();
                Number resultCode = (Number) resBody.get("resultCode");
                if (resultCode != null && resultCode.intValue() == 0) {
                    String payUrl = (String) resBody.get("payUrl");
                    System.out.println("[MoMo] Payment link created successfully: " + payUrl);
                    return ResponseEntity.ok(Map.of("payUrl", payUrl, "orderId", orderId));
                } else {
                    System.err.println("[MoMo API Error] " + resBody);
                    return ResponseEntity.badRequest().body(Map.of(
                            "message", resBody.getOrDefault("message", "Failed to initiate MoMo payment"),
                            "details", resBody
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("[Initiate MoMo Error] " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Server error initiating MoMo payment"));
    }

    @PostMapping("/deposit/momo/mock-confirm")
    public ResponseEntity<?> mockConfirmMomoPayment(
            @AuthenticationPrincipal User authUser,
            @RequestBody Map<String, Object> body) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        String orderId = (String) body.get("orderId");
        Number amountNum = (Number) body.get("amount");

        if (orderId == null || amountNum == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "OrderId and amount are required"));
        }

        Optional<WalletTransaction> existingTx = transactionRepository.findByDescriptionPattern(orderId);
        if (existingTx.isPresent()) {
            User user = userRepository.findById(authUser.getId()).orElseThrow();
            return ResponseEntity.ok(Map.of("walletBalance", user.getWalletBalance(), "alreadyProcessed", true));
        }

        System.out.println("[MoMo Mock Confirm] Depositing amount for order: " + amountNum + ", orderId: " + orderId);

        WalletService.WalletResult result = walletService.deposit(
                authUser.getId(),
                amountNum.doubleValue(),
                "Nap tien qua Vi dien tu MoMo (Ma giao dich: " + orderId + ")"
        );

        return ResponseEntity.ok(Map.of("walletBalance", result.user.getWalletBalance(), "transaction", result.transaction));
    }

    @PostMapping("/deposit/bank/confirm")
    public ResponseEntity<?> confirmBankDeposit(
            @AuthenticationPrincipal User authUser,
            @RequestBody Map<String, Object> body) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Number amountNum = (Number) body.get("amount");
        String referenceCode = (String) body.get("referenceCode");

        if (amountNum == null || amountNum.doubleValue() < 100000 || referenceCode == null || referenceCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số tiền nạp tối thiểu là 100,000 VND và mã tham chiếu là bắt buộc!"));
        }

        Optional<WalletTransaction> existingTx = transactionRepository.findByDescriptionPattern(referenceCode);
        if (existingTx.isPresent()) {
            User user = userRepository.findById(authUser.getId()).orElseThrow();
            return ResponseEntity.ok(Map.of("walletBalance", user.getWalletBalance(), "alreadyProcessed", true));
        }

        System.out.println("[Bank Deposit Confirm] Depositing amount: " + amountNum + ", reference: " + referenceCode);

        WalletService.WalletResult result = walletService.deposit(
                authUser.getId(),
                amountNum.doubleValue(),
                "Nap tien qua Chuyen khoan Ngan hang (Ma VietQR: " + referenceCode + ")"
        );

        return ResponseEntity.ok(Map.of("walletBalance", result.user.getWalletBalance(), "transaction", result.transaction));
    }

    @PostMapping("/momo-ipn")
    public ResponseEntity<?> momoIPN(@RequestBody Map<String, Object> data) {
        try {
            System.out.println("[MoMo Webhook IPN Received] " + data);

            // Reconstruct and verify signature
            String rawSignature = "accessKey=" + momoAccessKey +
                    "&amount=" + data.get("amount") +
                    "&extraData=" + data.get("extraData") +
                    "&message=" + data.get("message") +
                    "&orderId=" + data.get("orderId") +
                    "&orderInfo=" + data.get("orderInfo") +
                    "&partnerCode=" + data.get("partnerCode") +
                    "&requestId=" + data.get("requestId") +
                    "&responseTime=" + data.get("responseTime") +
                    "&resultCode=" + data.get("resultCode") +
                    "&transId=" + data.get("transId");

            String computedSignature = generateMomoSignature(rawSignature, momoSecretKey);

            if (!computedSignature.equals(data.get("signature"))) {
                System.err.println("[MoMo Webhook Signature Mismatch]");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Signature mismatch"));
            }

            Number resultCode = (Number) data.get("resultCode");
            if (resultCode != null && resultCode.intValue() == 0) {
                String orderId = (String) data.get("orderId");
                String[] parts = orderId.split("_");
                if (parts.length > 1) {
                    UUID userId = UUID.fromString(parts[1]);
                    
                    Optional<WalletTransaction> existingTx = transactionRepository.findByDescriptionPattern(orderId);
                    if (existingTx.isEmpty()) {
                        Number amount = (Number) data.get("amount");
                        walletService.deposit(
                                userId,
                                amount.doubleValue(),
                                "Nap tien qua Vi dien tu MoMo (Giao dich that: " + orderId + ")"
                        );
                        System.out.println("[MoMo IPN] Successfully credited user: " + userId);
                    }
                }
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            System.err.println("[MoMo IPN Error] " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
