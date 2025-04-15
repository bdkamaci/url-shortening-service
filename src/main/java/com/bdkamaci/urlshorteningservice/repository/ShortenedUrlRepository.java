package com.bdkamaci.urlshorteningservice.repository;

import com.bdkamaci.urlshorteningservice.model.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, Long> {
    Optional<ShortenedUrl> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
}
