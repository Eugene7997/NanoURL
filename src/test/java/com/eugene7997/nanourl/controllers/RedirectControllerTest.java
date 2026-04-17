package com.eugene7997.nanourl.controllers;

import com.eugene7997.nanourl.entities.ShortUrl;
import com.eugene7997.nanourl.services.UrlService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {

    @Mock
    private UrlService urlService;

    @InjectMocks
    private RedirectController redirectController;

    @Test
    void redirect_existingCode_returns302WithLocationHeader() {
        ShortUrl url = new ShortUrl("abc1234", "https://example.com", null);
        when(urlService.lookupActive("abc1234")).thenReturn(Optional.of(url));

        ResponseEntity<Void> response = redirectController.redirect("abc1234");

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("https://example.com", response.getHeaders().getFirst(HttpHeaders.LOCATION));
    }

    @Test
    void redirect_existingCode_registersHit() {
        ShortUrl url = new ShortUrl("abc1234", "https://example.com", null);
        when(urlService.lookupActive("abc1234")).thenReturn(Optional.of(url));

        redirectController.redirect("abc1234");

        verify(urlService, times(1)).registerHit("abc1234");
    }

    @Test
    void redirect_nonexistentCode_returns404() {
        when(urlService.lookupActive("missing")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = redirectController.redirect("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void redirect_nonexistentCode_doesNotRegisterHit() {
        when(urlService.lookupActive("missing")).thenReturn(Optional.empty());

        redirectController.redirect("missing");

        verify(urlService, never()).registerHit(any());
    }
}
