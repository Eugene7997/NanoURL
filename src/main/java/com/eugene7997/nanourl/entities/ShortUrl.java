package com.eugene7997.nanourl.entities;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "short_urls", indexes = {
    @Index(name = "idx_code_unique", columnList = "code", unique = true)
})
@Getter
@NoArgsConstructor
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 32)
    @Setter
    private String code;

    @Column(name = "target_url", nullable = false, length = 2048)
    @Setter
    private String targetUrl;

    @Column
    @Setter
    private Instant expiresAt;

    @Column(nullable = false)
    @Setter
    private long hits;

    @Column
    @Setter
    private Instant lastAccessedAt;

    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt = Instant.now();

    @Builder
    public ShortUrl(String code, String targetUrl, Instant expiresAt) {
        this.code = code;
        this.targetUrl = targetUrl;
        this.expiresAt = expiresAt;
    }
}
