DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS account;
CREATE TABLE account (
    owner   TEXT PRIMARY KEY,
    balance BIGINT NOT NULL CHECK (balance >= 0)
);
CREATE TABLE audit_log (
    id      BIGSERIAL PRIMARY KEY,
    message TEXT NOT NULL
);
