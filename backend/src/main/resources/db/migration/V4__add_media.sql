CREATE TABLE IF NOT EXISTS guide_media (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL UNIQUE,
    original_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    content_type VARCHAR(100),
    size BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    guide_id BIGINT NOT NULL REFERENCES guides(id) ON DELETE CASCADE
);
