package com.bdkamaci.urlshorteningservice.integration;

import com.bdkamaci.urlshorteningservice.dto.request.ShortenUrlRequest;
import com.bdkamaci.urlshorteningservice.dto.response.ShortenUrlResponse;
import com.bdkamaci.urlshorteningservice.model.ShortenedUrl;
import com.bdkamaci.urlshorteningservice.repository.ShortenedUrlRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UrlShortenerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShortenedUrlRepository repository;

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    void endToEndTest() throws Exception {
        // 1. Create a short URL
        ShortenUrlRequest createRequest = new ShortenUrlRequest();
        createRequest.setUrl("https://www.example.com/integration/test");

        MvcResult createResult = mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("https://www.example.com/integration/test"))
                .andExpect(jsonPath("$.shortCode").isNotEmpty())
                .andReturn();

        String createResponseJson = createResult.getResponse().getContentAsString();
        ShortenUrlResponse createResponse = objectMapper.readValue(createResponseJson, ShortenUrlResponse.class);
        String shortCode = createResponse.getShortCode();

        // 2. Retrieve the original URL
        mockMvc.perform(get("/shorten/{shortCode}", shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://www.example.com/integration/test"))
                .andExpect(jsonPath("$.shortCode").value(shortCode));

        // 3. Get statistics (should have access count = 0 initially)
        mockMvc.perform(get("/shorten/{shortCode}/stats", shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://www.example.com/integration/test"))
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.accessCount").value(1)); // First access after creation

        // 4. Update the URL
        ShortenUrlRequest updateRequest = new ShortenUrlRequest();
        updateRequest.setUrl("https://www.example.com/integration/updated");

        mockMvc.perform(put("/shorten/{shortCode}", shortCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://www.example.com/integration/updated"))
                .andExpect(jsonPath("$.shortCode").value(shortCode));

        // 5. Get statistics again (should be incremented)
        mockMvc.perform(get("/shorten/{shortCode}/stats", shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://www.example.com/integration/updated"))
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.accessCount").value(2)); // Second access after update

        // 6. Delete the URL
        mockMvc.perform(delete("/shorten/{shortCode}", shortCode))
                .andExpect(status().isNoContent());

        // 7. Verify the URL is deleted
        mockMvc.perform(get("/shorten/{shortCode}", shortCode))
                .andExpect(status().isNotFound());
    }

    @Test
    void createShortUrl_WithInvalidUrl_ShouldReturnBadRequest() throws Exception {
        // Empty URL
        ShortenUrlRequest invalidRequest = new ShortenUrlRequest();
        invalidRequest.setUrl("");

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // No URL property
        String invalidJson = "{}";
        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUrlStatistics_NonExistentShortCode_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/shorten/nonexistent/stats"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUrl_NonExistentShortCode_ShouldReturnNotFound() throws Exception {
        ShortenUrlRequest updateRequest = new ShortenUrlRequest();
        updateRequest.setUrl("https://www.example.com/updated");

        mockMvc.perform(put("/shorten/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUrl_NonExistentShortCode_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/shorten/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shortCodeGeneration_ShouldBeUnique() throws Exception {
        // Create multiple URLs and check for unique short codes
        for (int i = 0; i < 5; i++) {
            ShortenUrlRequest request = new ShortenUrlRequest();
            request.setUrl("https://www.example.com/test/" + i);

            MvcResult result = mockMvc.perform(post("/shorten")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            ShortenUrlResponse response = objectMapper.readValue(responseJson, ShortenUrlResponse.class);

            assertNotNull(response.getShortCode());
            assertTrue(response.getShortCode().length() >= 6);
        }

        // Verify all short codes are unique
        long count = repository.count();
        long uniqueCount = repository.findAll().stream()
                .map(ShortenedUrl::getShortCode)
                .distinct()
                .count();

        assertEquals(count, uniqueCount);
    }
}
