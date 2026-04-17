package com.eugene7997.nanourl.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.eugene7997.nanourl.dtos.CreateShortUrlRequest;
import com.eugene7997.nanourl.entities.ShortUrl;
import com.eugene7997.nanourl.repositories.ShortUrlRepository;

@Service
public class UrlService {

    private final ShortUrlRepository repository;
    private final ShortCodeService codeService;

    public UrlService(ShortUrlRepository repository, ShortCodeService codeService) {
        this.repository = repository;
        this.codeService = codeService;
    }

    public ShortUrl create(CreateShortUrlRequest request) {
        String target = normalizeUrl(request.getUrl());
        String code = codeService.validateCustomAlias(request.getAlias())
                .orElseGet(codeService::generateUniqueCode);

        Instant expiresAt = null;
        if (request.getExpiryDays() != null) {
            expiresAt = Instant.now().plus(request.getExpiryDays(), ChronoUnit.DAYS);
        }

        boolean isCustomAlias = request.getAlias() != null && !request.getAlias().isBlank();
        for (int attempt = 0; attempt < 10; attempt++) {
            String attemptCode = (attempt == 0) ? code : codeService.generateUniqueCode();
            try {
                return repository.save(new ShortUrl(attemptCode, target, expiresAt));
            }
            catch (DataIntegrityViolationException e) {
                if (isCustomAlias) {
                    throw new IllegalArgumentException("Alias already taken");
                }
                // generated code collision — retry with a new code
            }
        }
        throw new IllegalStateException("Failed to generate a unique short code after retries");
    }

    public Optional<ShortUrl> lookupActive(String code) {
        return repository.findByCode(code)
                .filter(url -> url.getExpiresAt() == null || url.getExpiresAt().isAfter(Instant.now()));
    }

    public void registerHit(String code) {
        repository.incrementHits(code, Instant.now());
    }

    private String normalizeUrl(String input) {
        try {
            URI uri = new URI(input.trim());
            if (uri.getScheme() == null) {
                uri = new URI("https://" + input.trim());
            }
            if (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https")) {
                throw new IllegalArgumentException("Only HTTP and HTTPS are supported");
            }
            return uri.normalize().toString();
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }
}
