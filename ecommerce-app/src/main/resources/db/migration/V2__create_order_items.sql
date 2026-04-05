CREATE TABLE order_items (
        id BIGSERIAL PRIMARY KEY,
        order_id UUID NOT NULL,
        product_id VARCHAR(50) NOT NULL,
        quantity INT NOT NULL,
        price NUMERIC(12,2) NOT NULL,

        CONSTRAINT fk_order
            FOREIGN KEY(order_id)
                REFERENCES orders(id)
                ON DELETE CASCADE
);