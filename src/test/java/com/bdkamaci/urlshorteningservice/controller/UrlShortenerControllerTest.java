package com.bdkamaci.urlshorteningservice.controller;

import com.bdkamaci.urlshorteningservice.dto.request.ShortenUrlRequest;
import com.bdkamaci.urlshorteningservice.dto.response.ShortenUrlResponse;
import com.bdkamaci.urlshorteningservice.dto.response.UrlStatisticsResponse;
import com.bdkamaci.urlshorteningservice.exception.ResourceNotFoundException;
import com.bdkamaci.urlshorteningservice.service.UrlShortenerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlShortenerController.class)
public class UrlShortenerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlShortenerService service;

    private ShortenUrlRequest request;
    private ShortenUrlResponse response;
    private UrlStatisticsResponse statsResponse;
    private final String TEST_URL = "https://www.example.com/long/url";
    private final String TEST_SHORT_CODE = "abc123";

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        request = new ShortenUrlRequest();
        request.setUrl(TEST_URL);

        response = ShortenUrlResponse.builder()
                .id("1")
                .url(TEST_URL)
                .shortCode(TEST_SHORT_CODE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        statsResponse = UrlStatisticsResponse.builder()
                .id("1")
                .url(TEST_URL)
                .shortCode(TEST_SHORT_CODE)
                .createdAt(now)
                .updatedAt(now)
                .accessCount(5L)
                .build();
    }

    @Test
    void createShortUrl_ShouldReturnCreatedStatus() throws Exception {
        // Arrange
        when(service.createShortUrl(any(ShortenUrlRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.url", is(TEST_URL)))
                .andExpect(jsonPath("$.shortCode", is(TEST_SHORT_CODE)));
    }

    @Test
    void createShortUrl_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ShortenUrlRequest invalidRequest = new ShortenUrlRequest();
        invalidRequest.setUrl(""); // Empty URL

        // Act & Assert
        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOriginalUrl_ShouldReturnOkStatus() throws Exception {
        // Arrange
        when(service.getUrlByShortCode(TEST_SHORT_CODE)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/shorten/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.url", is(TEST_URL)))
                .andExpect(jsonPath("$.shortCode", is(TEST_SHORT_CODE)));
    }

    @Test
    void getOriginalUrl_WithNonExistentCode_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(service.getUrlByShortCode(TEST_SHORT_CODE))
                .thenThrow(new ResourceNotFoundException("URL not found"));

        // Act & Assert
        mockMvc.perform(get("/shorten/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUrl_ShouldReturnOkStatus() throws Exception {
        // Arrange
        ShortenUrlResponse updatedResponse = ShortenUrlResponse.builder()
                .id("1")
                .url("https://www.example.com/updated/url")
                .shortCode(TEST_SHORT_CODE)
                .createdAt(response.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        ShortenUrlRequest updateRequest = new ShortenUrlRequest();
        updateRequest.setUrl("https://www.example.com/updated/url");

        when(service.updateUrl(eq(TEST_SHORT_CODE), any(ShortenUrlRequest.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/shorten/{shortCode}", TEST_SHORT_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.url", is("https://www.example.com/updated/url")))
                .andExpect(jsonPath("$.shortCode", is(TEST_SHORT_CODE)));
    }

    @Test
    void updateUrl_WithNonExistentCode_ShouldReturnNotFound() throws Exception {
        // Arrange
        ShortenUrlRequest updateRequest = new ShortenUrlRequest();
        updateRequest.setUrl("https://www.example.com/updated/url");

        when(service.updateUrl(eq(TEST_SHORT_CODE), any(ShortenUrlRequest.class)))
                .thenThrow(new ResourceNotFoundException("URL not found"));

        // Act & Assert
        mockMvc.perform(put("/shorten/{shortCode}", TEST_SHORT_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUrl_ShouldReturnNoContentStatus() throws Exception {
        // Arrange
        doNothing().when(service).deleteUrl(TEST_SHORT_CODE);

        // Act & Assert
        mockMvc.perform(delete("/shorten/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUrl_WithNonExistentCode_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("URL not found"))
                .when(service).deleteUrl(TEST_SHORT_CODE);

        // Act & Assert
        mockMvc.perform(delete("/shorten/{shortCode}", TEST_SHORT_CODE))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUrlStatistics_ShouldReturnOkStatus() throws Exception {
        // Arrange
        when(service.getUrlStatistics(TEST_SHORT_CODE)).thenReturn(statsResponse);

        // Act & Assert
        mockMvc.perform(get("/shorten/{shortCode}/stats", TEST_SHORT_CODE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.url", is(TEST_URL)))
                .andExpect(jsonPath("$.shortCode", is(TEST_SHORT_CODE)))
                .andExpect(jsonPath("$.accessCount", is(5)));
    }

    @Test
    void getUrlStatistics_WithNonExistentCode_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(service.getUrlStatistics(TEST_SHORT_CODE))
                .thenThrow(new ResourceNotFoundException("URL not found"));

        // Act & Assert
        mockMvc.perform(get("/shorten/{shortCode}/stats", TEST_SHORT_CODE))
                .andExpect(status().isNotFound());
    }
}
