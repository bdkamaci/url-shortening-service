package com.bdkamaci.urlshorteningservice.service;

import com.bdkamaci.urlshorteningservice.dto.request.ShortenUrlRequest;
import com.bdkamaci.urlshorteningservice.dto.response.ShortenUrlResponse;
import com.bdkamaci.urlshorteningservice.dto.response.UrlStatisticsResponse;

public interface UrlShortenerService {
    ShortenUrlResponse createShortUrl(ShortenUrlRequest request);
    ShortenUrlResponse getUrlByShortCode(String shortCode);
    ShortenUrlResponse updateUrl(String shortCode, ShortenUrlRequest request);
    void deleteUrl(String shortCode);
    UrlStatisticsResponse getUrlStatistics(String shortCode);
}
