package com.eugene7997.nanourl.dtos;

import java.time.Instant;

import com.eugene7997.nanourl.entities.ShortUrl;

public record ShortUrlResponse(
    String code,
    String targetUrl,
    Instant lastAccessedAt,
    Instant createdAt
) {
    public static ShortUrlResponse from(ShortUrl url) {
        return new ShortUrlResponse(
            url.getCode(), url.getTargetUrl(),
            url.getLastAccessedAt(), url.getCreatedAt()
        );
    }
}
