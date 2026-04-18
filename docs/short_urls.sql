CREATE TABLE short_urls (                                                                                                                                         
    id          BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,                                                                                               code        VARCHAR(32)     NOT NULL,                                                                                                                         
    target_url  VARCHAR(2048)   NOT NULL,                                                                                                                         
    expires_at  TIMESTAMPTZ,                                                                                                                                      
    hits        BIGINT          NOT NULL DEFAULT 0,
    last_accessed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT idx_code_unique UNIQUE (code)
);