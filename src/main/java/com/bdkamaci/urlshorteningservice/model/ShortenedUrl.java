package com.bdkamaci.urlshorteningservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shortened_urls", uniqueConstraints = {@UniqueConstraint(columnNames = "shortCode")})
public class ShortenedUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "URL is too long")
    @Column(length = 2048)
    private String url;

    @NotBlank(message = "Short code is required")
    @Size(min = 6, max = 10, message = "Short code must be between 6 and 10 characters")
    @Column(unique = true)
    private String shortCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    private Long accessCount = 0L;
}
