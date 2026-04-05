CREATE TABLE idempotency_keys (
     id VARCHAR(100) PRIMARY KEY,
     response TEXT,
     status VARCHAR(20), -- PROCESSING, COMPLETED
     created_at TIMESTAMP DEFAULT NOW()
);

DELETE FROM idempotency_keys WHERE created_at < NOW() - INTERVAL '24 hours';