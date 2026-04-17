package com.eugene7997.nanourl.services;

import com.eugene7997.nanourl.repositories.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortCodeServiceTest {

    @Mock
    private ShortUrlRepository repository;

    @InjectMocks
    private ShortCodeService service;

    @Test
    void validateCustomAlias_null_returnsEmpty() {
        assertThat(service.validateCustomAlias(null)).isEmpty();
    }

    @Test
    void validateCustomAlias_blank_returnsEmpty() {
        assertThat(service.validateCustomAlias("   ")).isEmpty();
    }

    @Test
    void validateCustomAlias_valid_returnsAlias() {
        when(repository.existsByCode("my_alias")).thenReturn(false);
        assertThat(service.validateCustomAlias("my_alias")).contains("my_alias");
    }

    @Test
    void validateCustomAlias_tooShort_throws() {
        assertThatThrownBy(() -> service.validateCustomAlias("ab"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3-32 characters");
    }

    @Test
    void validateCustomAlias_invalidChars_throws() {
        assertThatThrownBy(() -> service.validateCustomAlias("bad alias!"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateCustomAlias_tooLong_throws() {
        assertThatThrownBy(() -> service.validateCustomAlias("a".repeat(33)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateCustomAlias_alreadyExists_throws() {
        when(repository.existsByCode("taken")).thenReturn(true);
        assertThatThrownBy(() -> service.validateCustomAlias("taken"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void validateCustomAlias_exactlyMinLength_valid() {
        when(repository.existsByCode("abc")).thenReturn(false);
        assertThat(service.validateCustomAlias("abc")).contains("abc");
    }

    @Test
    void validateCustomAlias_exactlyMaxLength_valid() {
        String alias = "a".repeat(32);
        when(repository.existsByCode(alias)).thenReturn(false);
        assertThat(service.validateCustomAlias(alias)).contains(alias);
    }

    @Test
    void generateUniqueCode_returns7CharBase62Code() {
        String code = service.generateUniqueCode();
        assertThat(code).hasSize(7).matches("[0-9A-Za-z]+");
    }
}
