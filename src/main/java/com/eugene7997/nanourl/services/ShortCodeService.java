package com.eugene7997.nanourl.services;

import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import com.eugene7997.nanourl.repositories.ShortUrlRepository;
import com.eugene7997.nanourl.utilities.Base62;

@Service
public class ShortCodeService {
    private static final int DEFAULT_LENGTH = 7;
    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,32}$");
    private final ShortUrlRepository repository;

    public ShortCodeService(ShortUrlRepository repository) {
        this.repository = repository;
    }

    public String generateUniqueCode() {
        return Base62.randomCode(DEFAULT_LENGTH);
    }

    public Optional<String> validateCustomAlias(String alias) {
        if (alias == null || alias.isBlank())
            return Optional.empty();
        if (!ALIAS_PATTERN.matcher(alias).matches()) {
            throw new IllegalArgumentException(
                    "Alias must be 3-32 characters long and contain only letters, digits, or underscores");
        }
        if (repository.existsByCode(alias)) {
            throw new IllegalArgumentException("Alias already exists");
        }
        return Optional.of(alias);
    }
}
