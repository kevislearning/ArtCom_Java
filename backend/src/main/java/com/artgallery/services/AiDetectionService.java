package com.artgallery.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@Service
public class AiDetectionService {

    @Value("${hf.token}")
    private String hfToken;

    @Value("${hf.space-url}")
    private String hfSpaceUrl;

    @Value("${ai.detection-threshold}")
    private double threshold;

    private final RestTemplate restTemplate = new RestTemplate();

    public static class DetectionResult {
        public final boolean isAIDetected;
        public final double aiProbability;

        public DetectionResult(boolean isAIDetected, double aiProbability) {
            this.isAIDetected = isAIDetected;
            this.aiProbability = aiProbability;
        }
    }

    public DetectionResult scanImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new DetectionResult(false, 0.0);
        }

        try {
            String predictUrl = hfSpaceUrl.endsWith("/") ? hfSpaceUrl + "predict" : hfSpaceUrl + "/predict";
            System.out.println("[AiDetectionService] Sending scanning request to: " + predictUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            if (hfToken != null && !hfToken.isEmpty() && !hfToken.contains("hf_")) {
                headers.set("Authorization", "Bearer " + hfToken);
            }

            // Tạo ByteArrayResource tùy chỉnh kèm theo tên file để đáp ứng yêu cầu của header content-disposition
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<?> response = restTemplate.postForEntity(predictUrl, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.getBody();
                System.out.println("[AiDetectionService] Success: " + result);

                if (result.containsKey("all_probs")) {
                    @SuppressWarnings("unchecked")
                    List<Double> allProbs = (List<Double>) result.get("all_probs");
                    if (allProbs != null && allProbs.size() > 1) {
                        // Phần tử tại index 1 đại diện cho xác suất ảnh được tạo bởi AI (Synthetic)
                        double aiProbability = allProbs.get(1);
                        boolean isAIDetected = aiProbability >= threshold;
                        System.out.println("[AiDetectionService] Probability: " + aiProbability + " (threshold is " + threshold + "). Flagged: " + isAIDetected);
                        return new DetectionResult(isAIDetected, aiProbability);
                    }
                }
            } else {
                System.err.println("[AiDetectionService] Request failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("[AiDetectionService] Error scanning artwork image: " + e.getMessage());
        }

        return new DetectionResult(false, 0.0);
    }
}
