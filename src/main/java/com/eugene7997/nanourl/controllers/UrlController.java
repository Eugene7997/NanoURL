package com.eugene7997.nanourl.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eugene7997.nanourl.dtos.CreateShortUrlRequest;
import com.eugene7997.nanourl.entities.ShortUrl;
import com.eugene7997.nanourl.services.UrlService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ShortUrl> create(@Valid @RequestBody CreateShortUrlRequest request) {
        ShortUrl result = service.create(request);
        URI location = URI.create("/api/urls/" + result.getCode());
        return ResponseEntity.created(location).body(result);
    }

    @GetMapping("/{code}")
    public ResponseEntity<ShortUrl> get(@PathVariable String code) {
        return service.lookupActive(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
