package com.kaan.ecommerce_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiBridgeService {

    private final RestTemplate restTemplate;
    private final String AI_SERVICE_URL = "https://ecommerce-ai-service-production.up.railway.app/analyze";

    public AiBridgeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyzeReviews(List<String> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return "No reviews yet";
        }

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("reviews", reviews);

            return restTemplate.postForObject(AI_SERVICE_URL, request, String.class);
        } catch (Exception e) {
            return "Review analysis is currently unavailable: " + e.getMessage();
        }
    }
}
