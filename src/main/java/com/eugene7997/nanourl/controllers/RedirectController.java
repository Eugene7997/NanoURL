package com.eugene7997.nanourl.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.eugene7997.nanourl.services.UrlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Tag(name = "Redirect", description = "Resolve a short code and redirect to the target URL")
public class RedirectController {
    private final UrlService service;

    public RedirectController(UrlService service) {
        this.service = service;
    }

    @GetMapping("/{code}")
    @Operation(summary = "Redirect to target URL and register a hit")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Redirecting"),
        @ApiResponse(responseCode = "404", description = "Not found or expired")
    })
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        return service.lookupActive(code)
                .<ResponseEntity<Void>>map(url -> {
                    log.info("Redirect code={} -> {}", code, url.getTargetUrl());
                    service.registerHit(url.getCode());
                    return ResponseEntity.status(302)
                            .header(HttpHeaders.LOCATION, url.getTargetUrl())
                            .build();
                })
                .orElseGet(() -> {
                    log.warn("Redirect miss for code={}", code);
                    return ResponseEntity.notFound().build();
                });
    }
}
