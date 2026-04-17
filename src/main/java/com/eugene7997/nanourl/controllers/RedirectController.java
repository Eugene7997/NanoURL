package com.eugene7997.nanourl.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.eugene7997.nanourl.services.UrlService;

@RestController
public class RedirectController {
    private final UrlService service;

    public RedirectController(UrlService service) {
        this.service = service;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        return service.lookupActive(code)
                .<ResponseEntity<Void>>map(url -> {
                    service.registerHit(url.getCode());
                    return ResponseEntity.status(302)
                            .header(HttpHeaders.LOCATION, url.getTargetUrl())
                            .build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
