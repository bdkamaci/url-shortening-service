package com.bdkamaci.urlshorteningservice.repository;

import com.bdkamaci.urlshorteningservice.model.ShortenedUrl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ShortenedUrlRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ShortenedUrlRepository repository;

    @Test
    void findByShortCode_ShouldReturnShortenedUrl() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .url("https://www.example.com/long/url")
                .shortCode("abc123")
                .createdAt(now)
                .updatedAt(now)
                .accessCount(0L)
                .build();

        entityManager.persist(shortenedUrl);
        entityManager.flush();

        // Act
        Optional<ShortenedUrl> found = repository.findByShortCode("abc123");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("https://www.example.com/long/url", found.get().getUrl());
        assertEquals("abc123", found.get().getShortCode());
    }

    @Test
    void findByShortCode_WithNonExistentCode_ShouldReturnEmpty() {
        // Act
        Optional<ShortenedUrl> found = repository.findByShortCode("nonexistent");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void existsByShortCode_WithExistingCode_ShouldReturnTrue() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .url("https://www.example.com/long/url")
                .shortCode("exists")
                .createdAt(now)
                .updatedAt(now)
                .accessCount(0L)
                .build();

        entityManager.persist(shortenedUrl);
        entityManager.flush();

        // Act
        boolean exists = repository.existsByShortCode("exists");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByShortCode_WithNonExistentCode_ShouldReturnFalse() {
        // Act
        boolean exists = repository.existsByShortCode("nonexistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_ShouldPersistShortenedUrl() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .url("https://www.example.com/save/test")
                .shortCode("savetest")
                .createdAt(now)
                .updatedAt(now)
                .accessCount(0L)
                .build();

        // Act
        ShortenedUrl saved = repository.save(shortenedUrl);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("https://www.example.com/save/test", saved.getUrl());
        assertEquals("savetest", saved.getShortCode());

        // Verify it was actually saved
        ShortenedUrl found = entityManager.find(ShortenedUrl.class, saved.getId());
        assertNotNull(found);
        assertEquals("savetest", found.getShortCode());
    }

    @Test
    void delete_ShouldRemoveShortenedUrl() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .url("https://www.example.com/delete/test")
                .shortCode("deletetest")
                .createdAt(now)
                .updatedAt(now)
                .accessCount(0L)
                .build();

        entityManager.persist(shortenedUrl);
        entityManager.flush();

        Long id = shortenedUrl.getId();

        // Act
        repository.delete(shortenedUrl);

        // Assert
        ShortenedUrl found = entityManager.find(ShortenedUrl.class, id);
        assertNull(found);
    }
}
