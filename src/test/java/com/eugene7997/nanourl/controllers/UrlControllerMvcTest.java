package com.eugene7997.nanourl.controllers;

import com.eugene7997.nanourl.services.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlController.class)
class UrlControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Test
    void create_blankUrl_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_missingUrl_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_expiryDaysTooLow_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://example.com\", \"expiryDays\": 0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_expiryDaysTooHigh_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://example.com\", \"expiryDays\": 366}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_invalidAliasPattern_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\": \"https://example.com\", \"alias\": \"bad alias!\"}"))
                .andExpect(status().isBadRequest());
    }
}
