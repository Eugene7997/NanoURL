DELETE FROM short_urls
WHERE expires_at IS NOT NULL AND expires_at < NOW();
