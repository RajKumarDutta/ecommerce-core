CREATE TABLE inventory_reservations (
        id UUID PRIMARY KEY,
        product_id VARCHAR(50) NOT NULL,
        order_id UUID NOT NULL,
        quantity INT NOT NULL,
        status VARCHAR(20), -- RESERVED, CONFIRMED, RELEASED
        created_at TIMESTAMP DEFAULT NOW()
);

DELETE FROM inventory_reservations
WHERE status = 'RESERVED'
  AND created_at < NOW() - INTERVAL '15 minutes';