CREATE TABLE products (
        id UUID PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        price NUMERIC(12,2) NOT NULL,
        available_quantity INT NOT NULL,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_products_name ON products(name);