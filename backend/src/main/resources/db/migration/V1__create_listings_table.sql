CREATE TABLE Listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_ID UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active', -- active , pending, sold, deleted ?
    image_URL VARCHAR(512), -- PLACEHOLDER FOR FUTURE S3 INTEGRATION
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
)