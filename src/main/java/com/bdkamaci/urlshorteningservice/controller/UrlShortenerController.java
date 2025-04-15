package com.bdkamaci.urlshorteningservice.controller;

import com.bdkamaci.urlshorteningservice.dto.request.ShortenUrlRequest;
import com.bdkamaci.urlshorteningservice.dto.response.ShortenUrlResponse;
import com.bdkamaci.urlshorteningservice.dto.response.UrlStatisticsResponse;
import com.bdkamaci.urlshorteningservice.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shorten")
@RequiredArgsConstructor
public class UrlShortenerController {
    private final UrlShortenerService urlShortenerService;

    @PostMapping
    public ResponseEntity<ShortenUrlResponse> createShortUrl(
            @Valid @RequestBody ShortenUrlRequest request
    ) {
        ShortenUrlResponse response = urlShortenerService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<ShortenUrlResponse> getOriginalUrl(
            @PathVariable String shortCode
    ) {
        ShortenUrlResponse response = urlShortenerService.getUrlByShortCode(shortCode);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{shortCode}")
    public ResponseEntity<ShortenUrlResponse> updateUrl(
            @PathVariable String shortCode,
            @Valid @RequestBody ShortenUrlRequest request
    ) {
        ShortenUrlResponse response = urlShortenerService.updateUrl(shortCode, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable String shortCode
    ) {
        urlShortenerService.deleteUrl(shortCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<UrlStatisticsResponse> getUrlStatistics(
            @PathVariable String shortCode
    ) {
        UrlStatisticsResponse response = urlShortenerService.getUrlStatistics(shortCode);
        return ResponseEntity.ok(response);
    }
}
