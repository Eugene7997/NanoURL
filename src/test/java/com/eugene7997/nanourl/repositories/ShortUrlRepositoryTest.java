package com.eugene7997.nanourl.repositories;

import com.eugene7997.nanourl.entities.ShortUrl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShortUrlRepositoryTest {

    @Autowired
    private ShortUrlRepository repository;

    @Test
    void findByCode_found() {
        repository.save(new ShortUrl("abc1234", "https://example.com", null));

        Optional<ShortUrl> result = repository.findByCode("abc1234");

        assertThat(result).isPresent();
        assertThat(result.get().getTargetUrl()).isEqualTo("https://example.com");
    }

    @Test
    void findByCode_notFound() {
        assertThat(repository.findByCode("missing")).isEmpty();
    }

    @Test
    void existsByCode_true() {
        repository.save(new ShortUrl("abc1234", "https://example.com", null));

        assertThat(repository.existsByCode("abc1234")).isTrue();
    }

    @Test
    void existsByCode_false() {
        assertThat(repository.existsByCode("nope")).isFalse();
    }

    @Test
    void incrementHits_updatesHitsAndLastAccessedAt() {
        repository.save(new ShortUrl("abc1234", "https://example.com", null));
        // H2 stores only microseconds not nanoseconds in JVM
        Instant before = Instant.now().truncatedTo(ChronoUnit.MICROS);

        repository.incrementHits("abc1234", before);

        ShortUrl updated = repository.findByCode("abc1234").orElseThrow();
        assertThat(updated.getHits()).isEqualTo(1);
        assertThat(updated.getLastAccessedAt()).isEqualTo(before);
    }

    @Test
    void incrementHits_multipleCallsAccumulate() {
        repository.save(new ShortUrl("abc1234", "https://example.com", null));

        repository.incrementHits("abc1234", Instant.now());
        repository.incrementHits("abc1234", Instant.now());

        assertThat(repository.findByCode("abc1234").orElseThrow().getHits()).isEqualTo(2);
    }

    @Test
    void incrementHits_unknownCode_affectsNoRows() {
        int affected = repository.incrementHits("nosuchcode", Instant.now());

        assertThat(affected).isZero();
    }
}
