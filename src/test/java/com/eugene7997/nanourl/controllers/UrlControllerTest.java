package com.eugene7997.nanourl.controllers;

import com.eugene7997.nanourl.dtos.CreateShortUrlRequest;
import com.eugene7997.nanourl.entities.ShortUrl;
import com.eugene7997.nanourl.services.UrlService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlControllerTest {

    @Mock
    private UrlService urlService;

    @InjectMocks
    private UrlController urlController;

    private CreateShortUrlRequest buildRequest(String url, String alias, Integer expiryDays) {
        CreateShortUrlRequest r = new CreateShortUrlRequest();
        r.setUrl(url);
        r.setAlias(alias);
        r.setExpiryDays(expiryDays);
        return r;
    }

    @Test
    void create_validRequest_returns201WithLocation() {
        ShortUrl saved = new ShortUrl("abc1234", "https://example.com", null);
        when(urlService.create(any())).thenReturn(saved);

        ResponseEntity<ShortUrl> response = urlController.create(buildRequest("https://example.com", null, null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/urls/abc1234");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("abc1234");
    }

    @Test
    void create_callsServiceWithRequest() {
        ShortUrl saved = new ShortUrl("abc1234", "https://example.com", null);
        when(urlService.create(any())).thenReturn(saved);

        CreateShortUrlRequest request = buildRequest("https://example.com", "myalias", 30);
        urlController.create(request);

        verify(urlService, times(1)).create(request);
    }

    @Test
    void get_existingCode_returns200WithBody() {
        ShortUrl url = new ShortUrl("abc1234", "https://example.com", null);
        when(urlService.lookupActive("abc1234")).thenReturn(Optional.of(url));

        ResponseEntity<ShortUrl> response = urlController.get("abc1234");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("abc1234");
        assertThat(response.getBody().getTargetUrl()).isEqualTo("https://example.com");
    }

    @Test
    void get_nonexistentCode_returns404() {
        when(urlService.lookupActive("missing")).thenReturn(Optional.empty());

        ResponseEntity<ShortUrl> response = urlController.get("missing");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }
}
