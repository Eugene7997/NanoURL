package com.eugene7997.nanourl.repositories;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.eugene7997.nanourl.entities.ShortUrl;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByCode(String code);

    boolean existsByCode(String code);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ShortUrl s SET s.hits = s.hits + 1, s.lastAccessedAt = :now WHERE s.code = :code")
    int incrementHits(@Param("code") String code, @Param("now") Instant now);
}