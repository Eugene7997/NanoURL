package com.eugene7997.nanourl.services;

import com.eugene7997.nanourl.dtos.CreateShortUrlRequest;
import com.eugene7997.nanourl.entities.ShortUrl;
import com.eugene7997.nanourl.repositories.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private ShortUrlRepository repository;

    @Mock
    private ShortCodeService codeService;

    @InjectMocks
    private UrlService service;

    private CreateShortUrlRequest buildRequest(String url, String alias, Integer expiryDays) {
        CreateShortUrlRequest r = new CreateShortUrlRequest();
        r.setUrl(url);
        r.setAlias(alias);
        r.setExpiryDays(expiryDays);
        return r;
    }

    @Test
    void create_generatedCode_savesAndReturns() {
        when(codeService.validateCustomAlias(null)).thenReturn(Optional.empty());
        when(codeService.generateUniqueCode()).thenReturn("abc1234");
        ShortUrl saved = new ShortUrl("abc1234", "https://example.com", null);
        when(repository.save(any())).thenReturn(saved);

        ShortUrl result = service.create(buildRequest("https://example.com", null, null));

        assertThat(result.getCode()).isEqualTo("abc1234");
        assertThat(result.getTargetUrl()).isEqualTo("https://example.com");
    }

    @Test
    void create_customAlias_usesAlias() {
        when(codeService.validateCustomAlias("mylink")).thenReturn(Optional.of("mylink"));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.create(buildRequest("https://example.com", "mylink", null));

        assertThat(result.getCode()).isEqualTo("mylink");
    }

    @Test
    void create_withExpiryDays_setsExpiresAt() {
        when(codeService.validateCustomAlias(null)).thenReturn(Optional.empty());
        when(codeService.generateUniqueCode()).thenReturn("abc1234");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.create(buildRequest("https://example.com", null, 30));

        assertThat(result.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void create_noExpiry_expiresAtIsNull() {
        when(codeService.validateCustomAlias(null)).thenReturn(Optional.empty());
        when(codeService.generateUniqueCode()).thenReturn("abc1234");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.create(buildRequest("https://example.com", null, null));

        assertThat(result.getExpiresAt()).isNull();
    }

    @Test
    void create_customAlias_collision_throwsIllegalArgument() {
        when(codeService.validateCustomAlias("taken")).thenReturn(Optional.of("taken"));
        when(repository.save(any())).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> service.create(buildRequest("https://example.com", "taken", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alias already taken");
    }

    @Test
    void create_normalizeUrl_prependsHttpsWhenNoScheme() {
        when(codeService.validateCustomAlias(null)).thenReturn(Optional.empty());
        when(codeService.generateUniqueCode()).thenReturn("abc1234");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.create(buildRequest("example.com", null, null));

        assertThat(result.getTargetUrl()).startsWith("https://");
    }

    @Test
    void create_ftpScheme_throwsIllegalArgument() {
        assertThatThrownBy(() -> service.create(buildRequest("ftp://example.com", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTP and HTTPS");
    }

    @Test
    void create_generatedCode_collision_retriesAndSucceeds() {
        when(codeService.validateCustomAlias(null)).thenReturn(Optional.empty());
        when(codeService.generateUniqueCode()).thenReturn("aaaaaaa").thenReturn("bbbbbbb");
        ShortUrl saved = new ShortUrl("bbbbbbb", "https://example.com", null);
        when(repository.save(any()))
                .thenThrow(DataIntegrityViolationException.class)
                .thenReturn(saved);

        ShortUrl result = service.create(buildRequest("https://example.com", null, null));

        assertThat(result.getCode()).isEqualTo("bbbbbbb");
        verify(codeService, times(2)).generateUniqueCode();
    }

    @Test
    void lookupActive_found_notExpired_returnsUrl() {
        ShortUrl url = new ShortUrl("abc1234", "https://example.com", Instant.now().plusSeconds(3600));
        when(repository.findByCode("abc1234")).thenReturn(Optional.of(url));

        assertThat(service.lookupActive("abc1234")).isPresent();
    }

    @Test
    void lookupActive_noExpiry_returnsUrl() {
        ShortUrl url = new ShortUrl("abc1234", "https://example.com", null);
        when(repository.findByCode("abc1234")).thenReturn(Optional.of(url));

        assertThat(service.lookupActive("abc1234")).isPresent();
    }

    @Test
    void lookupActive_expired_returnsEmpty() {
        ShortUrl url = new ShortUrl("abc1234", "https://example.com", Instant.now().minusSeconds(1));
        when(repository.findByCode("abc1234")).thenReturn(Optional.of(url));

        assertThat(service.lookupActive("abc1234")).isEmpty();
    }

    @Test
    void lookupActive_notFound_returnsEmpty() {
        when(repository.findByCode("missing")).thenReturn(Optional.empty());

        assertThat(service.lookupActive("missing")).isEmpty();
    }

    @Test
    void registerHit_callsIncrementHits() {
        service.registerHit("abc1234");

        verify(repository).incrementHits(eq("abc1234"), any(Instant.class));
    }
}
