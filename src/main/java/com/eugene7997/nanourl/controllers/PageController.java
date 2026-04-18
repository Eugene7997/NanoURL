package com.eugene7997.nanourl.controllers;

import com.eugene7997.nanourl.dtos.CreateShortUrlRequest;
import com.eugene7997.nanourl.entities.ShortUrl;
import com.eugene7997.nanourl.services.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    private final UrlService urlService;

    public PageController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/shorten")
    public String shorten(
            @RequestParam String url,
            @RequestParam(required = false) String alias,
            @RequestParam(required = false) Integer expiryDays,
            Model model,
            HttpServletRequest request) {
        try {
            CreateShortUrlRequest req = new CreateShortUrlRequest();
            req.setUrl(url);
            if (alias != null && !alias.isBlank()) req.setAlias(alias);
            req.setExpiryDays(expiryDays);

            ShortUrl result = urlService.create(req);
            model.addAttribute("shortUrl", result);
            model.addAttribute("shortLink", baseUrl(request) + "/" + result.getCode());
            return "fragments/results :: shorten-result";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "fragments/results :: error-result";
        }
    }

    @GetMapping("/lookup")
    public String lookup(
            @RequestParam String code,
            Model model,
            HttpServletRequest request) {
        return urlService.lookupActive(code)
                .map(shortUrl -> {
                    model.addAttribute("shortUrl", shortUrl);
                    model.addAttribute("shortLink", baseUrl(request) + "/" + shortUrl.getCode());
                    return "fragments/results :: lookup-result";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Short URL not found or has expired");
                    return "fragments/results :: error-result";
                });
    }

    private String baseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        if ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443)) {
            return scheme + "://" + host;
        }
        return scheme + "://" + host + ":" + port;
    }
}
