package com.bdkamaci.urlshorteningservice.service;

import com.bdkamaci.urlshorteningservice.dto.request.ShortenUrlRequest;
import com.bdkamaci.urlshorteningservice.dto.response.ShortenUrlResponse;
import com.bdkamaci.urlshorteningservice.dto.response.UrlStatisticsResponse;
import com.bdkamaci.urlshorteningservice.exception.ResourceNotFoundException;
import com.bdkamaci.urlshorteningservice.model.ShortenedUrl;
import com.bdkamaci.urlshorteningservice.repository.ShortenedUrlRepository;
import com.bdkamaci.urlshorteningservice.service.impl.UrlShortenerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UrlShortenerServiceTest {
    @Mock
    private ShortenedUrlRepository repository;

    @InjectMocks
    private UrlShortenerServiceImpl service;

    private ShortenedUrl shortenedUrl;
    private ShortenUrlRequest request;
    private final String TEST_URL = "https://www.example.com/long/url";
    private final String TEST_SHORT_CODE = "abc123";

    @BeforeEach
    void setUp() {
        // Initialize test data
        LocalDateTime now = LocalDateTime.now();

        shortenedUrl = ShortenedUrl.builder()
                .id(1L)
                .url(TEST_URL)
                .shortCode(TEST_SHORT_CODE)
                .createdAt(now)
                .updatedAt(now)
                .accessCount(0L)
                .build();

        request = new ShortenUrlRequest();
        request.setUrl(TEST_URL);
    }

    @Test
    void createShortUrl_ShouldReturnShortenUrlResponse() {
        // Arrange
        when(repository.save(any(ShortenedUrl.class))).thenReturn(shortenedUrl);

        // Act
        ShortenUrlResponse response = service.createShortUrl(request);

        // Assert
        assertNotNull(response);
        assertEquals(shortenedUrl.getId().toString(), response.getId());
        assertEquals(shortenedUrl.getUrl(), response.getUrl());
        assertEquals(shortenedUrl.getShortCode(), response.getShortCode());

        verify(repository, times(1)).save(any(ShortenedUrl.class));
    }

    @Test
    void getUrlByShortCode_ShouldReturnShortenUrlResponse() {
        // Arrange
        when(repository.findByShortCode(TEST_SHORT_CODE)).thenReturn(Optional.of(shortenedUrl));

        // Act
        ShortenUrlResponse response = service.getUrlByShortCode(TEST_SHORT_CODE);

        // Assert
        assertNotNull(response);
        assertEquals(shortenedUrl.getId().toString(), response.getId());
        assertEquals(shortenedUrl.getUrl(), response.getUrl());
        assertEquals(shortenedUrl.getShortCode(), response.getShortCode());

        verify(repository, times(1)).findByShortCode(TEST_SHORT_CODE);
    }

    @Test
    void getUrlByShortCode_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(repository.findByShortCode(TEST_SHORT_CODE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            service.getUrlByShortCode(TEST_SHORT_CODE);
        });

        verify(repository, times(1)).findByShortCode(TEST_SHORT_CODE);
    }

    @Test
    void updateUrl_ShouldReturnUpdatedResponse() {
        // Arrange
        ShortenedUrl updatedUrl = ShortenedUrl.builder()
                .id(shortenedUrl.getId())
                .url("https://www.example.com/updated/url")
                .shortCode(shortenedUrl.getShortCode())
                .createdAt(shortenedUrl.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .accessCount(shortenedUrl.getAccessCount())
                .build();

        when(repository.findByShortCode(TEST_SHORT_CODE)).thenReturn(Optional.of(shortenedUrl));
        when(repository.save(any(ShortenedUrl.class))).thenReturn(updatedUrl);

        ShortenUrlRequest updateRequest = new ShortenUrlRequest();
        updateRequest.setUrl("https://www.example.com/updated/url");

        // Act
        ShortenUrlResponse response = service.updateUrl(TEST_SHORT_CODE, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(updatedUrl.getId().toString(), response.getId());
        assertEquals(updatedUrl.getUrl(), response.getUrl());
        assertEquals(updatedUrl.getShortCode(), response.getShortCode());

        verify(repository, times(1)).findByShortCode(TEST_SHORT_CODE);
        verify(repository, times(1)).save(any(ShortenedUrl.class));
    }

    @Test
    void updateUrl_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(repository.findByShortCode(TEST_SHORT_CODE)).thenReturn(Optional.empty());

        ShortenUrlRequest updateRequest = new ShortenUrlRequest();
        updateRequest.setUrl("https://www.example.com/updated/url");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            service.updateUrl(TEST_SHORT_CODE, updateRequest);
        });

        verify(repository, times(1)).findByShortCode(TEST_SHORT_CODE);
        verify(repository, never()).save(any(ShortenedUrl.class));
    }

    @Test
    void deleteUrl_ShouldDeleteSuccessfully() {
        // Arrange
        when(repository.findByShortCode(TEST_SHORT_CODE)).thenReturn(Optional.of(shortenedUrl));
        doNothing().when(repository).delete(any(ShortenedUrl.class));

        // Act
        service.deleteUrl(TEST_SHORT_CODE);

        // Assert
        verify(repository, times(1)).findByShortCode(TEST_SHORT_CODE);
        verify(repository, times(1)).delete(shortenedUrl);
    }

    @Test
    void deleteUrl_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(repository.findByShortCode(TEST_SHORT_CODE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            service.deleteUrl(TEST_SHORT_CODE);
        });

        verify(repository, times(1)).findByShortCode(TEST_SHORT_CODE);
        verify(repository, never()).delete(any(ShortenedUrl.class));
    }

    @Test
    void getUrlStatistics_ShouldReturnStatistics() {
        // Arrange
        ShortenedUrl urlWithStats = ShortenedUrl.builder()
                .id(shortenedUrl.getId())
                .url(shortenedUrl.getUrl())
                .shortCode(shortenedUrl.getShortCode())
                .createdAt(shortenedUrl.getCreatedAt())
                .updatedAt(shortenedUrl.getUpdatedAt())
                .accessCount(5L)
                .build();

        when(repository.findByShortCode(TEST_SHORT_CODE)).thenReturn(Optional.of(urlWithStats));
        when(repository.save(any(ShortenedUrl.class))).thenReturn(urlWithStats);

        // Act
        UrlStatisticsResponse response = service.getUrlStatistics(TEST_SHORT_CODE);

        // Assert
        assertNotNull(response);
        assertEquals(urlWithStats.getId().toString(), response.getId());
        assertEquals(urlWithStats.getUrl(), response.getUrl());
        assertEquals(urlWithStats.getShortCode(), response.getShortCode());
        assertEquals(urlWithStats.getAccessCount(), response.getAccessCount());

        verify(repository, times(1)).findByShortCode(TEST_SHORT_CODE);
        verify(repository, times(1)).save(any(ShortenedUrl.class));
    }

    @Test
    void getUrlStatistics_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(repository.findByShortCode(TEST_SHORT_CODE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            service.getUrlStatistics(TEST_SHORT_CODE);
        });

        verify(repository, times(1)).findByShortCode(TEST_SHORT_CODE);
        verify(repository, never()).save(any(ShortenedUrl.class));
    }
}
