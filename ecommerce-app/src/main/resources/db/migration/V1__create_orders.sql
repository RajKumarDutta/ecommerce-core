CREATE TABLE orders (
        id UUID PRIMARY KEY,
        status VARCHAR(30) NOT NULL,
        total_amount NUMERIC(12,2) NOT NULL,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
        version BIGINT DEFAULT 0
);