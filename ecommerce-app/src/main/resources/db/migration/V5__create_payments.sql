CREATE TABLE payments (
                          id UUID PRIMARY KEY,
                          order_id UUID NOT NULL,
                          merchant_order_id VARCHAR(100) NOT NULL,
                          pg_transaction_id VARCHAR(100), -- Captured from getPaymentDetails()
                          provider VARCHAR(50) NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          payment_mode VARCHAR(50),      -- Captured from getPaymentDetails()
                          amount NUMERIC(12,2) NOT NULL,
                          raw_response TEXT,              -- Audit trail
                          created_at TIMESTAMP DEFAULT NOW(),

                          CONSTRAINT unique_merchant_order_id UNIQUE (merchant_order_id)
);

ALTER TABLE payments ADD COLUMN version INTEGER DEFAULT 0;

CREATE INDEX idx_payment_merchant_order_id ON payments(merchant_order_id);
CREATE INDEX idx_payment_status ON payments(status);