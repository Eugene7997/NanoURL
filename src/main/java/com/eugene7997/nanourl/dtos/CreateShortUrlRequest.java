package com.eugene7997.nanourl.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@Schema(description = "Request body for creating a short URL")
public class CreateShortUrlRequest {
    @NotBlank(message = "URL is required")
    @Size(max = 2048, message = "URL is too long")
    @URL(message = "Must be a valid URL")
    @Schema(description = "Target URL to shorten", example = "https://example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;

    @Pattern(regexp = "^[A-Za-z0-9_]{3,32}$", message = "Alias must be 3-32 characters long and contain only letters, digits, or underscores")
    @Schema(description = "Optional custom alias (3-32 chars, [A-Za-z0-9_])", example = "my-alias")
    private String alias;

    @Min(value = 1, message = "Expiry must be at least 1 day")
    @Max(value = 365, message = "Expiry cannot exceed 365 days")
    @Schema(description = "Optional expiry in days (1-365)", example = "30")
    private Integer expiryDays;
}
