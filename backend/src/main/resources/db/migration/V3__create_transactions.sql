CREATE TABLE transactions (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    -- No ON DELETE here: Postgres refuses to delete a category that transactions still use (US-6 backstop).
    category_id BIGINT        NOT NULL REFERENCES categories(id),
    -- NUMERIC stores exact decimals; 12 digits in total, 2 after the point (max 9,999,999,999.99).
    amount      NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    date        DATE          NOT NULL,
    note        VARCHAR(255),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- The list screen asks "this user's transactions, newest first": this index answers it directly.
CREATE INDEX idx_transactions_user_date ON transactions (user_id, date DESC, id DESC);
-- Postgres doesn't index foreign keys automatically; this one serves "is this category still used?".
CREATE INDEX idx_transactions_category ON transactions (category_id);
