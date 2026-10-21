CREATE TABLE categories (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name    VARCHAR(50) NOT NULL,
    type    VARCHAR(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE'))
);

-- One name per user, ignoring case ("Food" and "food" clash).
-- user_id is the first column, so this index also speeds up "all categories of user X".
CREATE UNIQUE INDEX uq_categories_user_name ON categories (user_id, lower(name));
