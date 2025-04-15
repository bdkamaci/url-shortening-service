package com.bdkamaci.urlshorteningservice.service.impl;

import com.bdkamaci.urlshorteningservice.dto.request.ShortenUrlRequest;
import com.bdkamaci.urlshorteningservice.dto.response.ShortenUrlResponse;
import com.bdkamaci.urlshorteningservice.dto.response.UrlStatisticsResponse;
import com.bdkamaci.urlshorteningservice.exception.ResourceNotFoundException;
import com.bdkamaci.urlshorteningservice.model.ShortenedUrl;
import com.bdkamaci.urlshorteningservice.repository.ShortenedUrlRepository;
import com.bdkamaci.urlshorteningservice.service.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class UrlShortenerServiceImpl implements UrlShortenerService {
    private final ShortenedUrlRepository repository;
    private static final int SHORT_CODE_LENGTH = 8;

    private String generateUniqueShortCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[6];
        random.nextBytes(bytes);

        String shortCode = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes)
                .substring(0, SHORT_CODE_LENGTH);

        // Ensure uniqueness
        while (repository.existsByShortCode(shortCode)) {
            random.nextBytes(bytes);
            shortCode = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(bytes)
                    .substring(0, SHORT_CODE_LENGTH);
        }

        return shortCode;
    }

    @Override
    public ShortenUrlResponse createShortUrl(ShortenUrlRequest request) {
        String shortCode = generateUniqueShortCode();
        LocalDateTime now = LocalDateTime.now();

        ShortenedUrl url = ShortenedUrl.builder()
                .url(request.getUrl())
                .shortCode(shortCode)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ShortenedUrl savedUrl = repository.save(url);

        return ShortenUrlResponse.builder()
                .id(savedUrl.getId().toString())
                .url(savedUrl.getUrl())
                .shortCode(savedUrl.getShortCode())
                .createdAt(savedUrl.getCreatedAt())
                .updatedAt(savedUrl.getUpdatedAt())
                .build();
    }

    @Override
    public ShortenUrlResponse getUrlByShortCode(String shortCode) {
        ShortenedUrl url = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        return ShortenUrlResponse.builder()
                .id(url.getId().toString())
                .url(url.getUrl())
                .shortCode(url.getShortCode())
                .createdAt(url.getCreatedAt())
                .updatedAt(url.getUpdatedAt())
                .build();
    }

    @Override
    public ShortenUrlResponse updateUrl(String shortCode, ShortenUrlRequest request) {
        ShortenedUrl url = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        url.setUrl(request.getUrl());
        url.setUpdatedAt(LocalDateTime.now());

        ShortenedUrl savedUrl = repository.save(url);

        return ShortenUrlResponse.builder()
                .id(savedUrl.getId().toString())
                .url(savedUrl.getUrl())
                .shortCode(savedUrl.getShortCode())
                .createdAt(savedUrl.getCreatedAt())
                .updatedAt(savedUrl.getUpdatedAt())
                .build();
    }

    @Override
    public void deleteUrl(String shortCode) {
        ShortenedUrl url = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        repository.delete(url);
    }

    @Override
    public UrlStatisticsResponse getUrlStatistics(String shortCode) {
        ShortenedUrl url = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        // Increment access count
        url.setAccessCount(url.getAccessCount() + 1);
        repository.save(url);

        return UrlStatisticsResponse.builder()
                .id(url.getId().toString())
                .url(url.getUrl())
                .shortCode(url.getShortCode())
                .createdAt(url.getCreatedAt())
                .updatedAt(url.getUpdatedAt())
                .accessCount(url.getAccessCount())
                .build();
    }
}
