-- =====================================================================
--  ATM Simulator - MySQL database script  (MySQL 8.0+)
--  Run:   mysql -u root -p < sql/atm_db.sql
--  or open this file in MySQL Workbench and execute it.
-- =====================================================================

DROP DATABASE IF EXISTS atm_db;
CREATE DATABASE atm_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE atm_db;

-- ---------------------------------------------------------------------
-- Account holders
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    full_name   VARCHAR(80)  NOT NULL,
    email       VARCHAR(120) NOT NULL,
    phone       CHAR(10)     NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    UNIQUE KEY uq_users_phone (phone)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Accounts (PIN stored only as PBKDF2-HMAC-SHA256 hash + per-account salt)
-- ---------------------------------------------------------------------
CREATE TABLE accounts (
    account_number  CHAR(12)       NOT NULL,
    user_id         BIGINT         NOT NULL,
    pin_hash        VARCHAR(64)    NOT NULL,
    pin_salt        VARCHAR(32)    NOT NULL,
    balance         DECIMAL(15,2)  NOT NULL DEFAULT 0.00,
    failed_attempts INT            NOT NULL DEFAULT 0,
    locked          TINYINT(1)     NOT NULL DEFAULT 0,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_number),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Transactions
-- ---------------------------------------------------------------------
CREATE TABLE transactions (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    account_number   CHAR(12)      NOT NULL,
    type             ENUM('DEPOSIT','WITHDRAWAL','TRANSFER_OUT','TRANSFER_IN') NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    balance_after    DECIMAL(15,2) NOT NULL,
    related_account  CHAR(12)      NULL,
    description      VARCHAR(160)  NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_tx_account_date (account_number, created_at),
    CONSTRAINT fk_tx_account FOREIGN KEY (account_number) REFERENCES accounts (account_number),
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Demo data (optional - you can also use the Signup screen)
--   Account 100000000001  PIN 4826   (Aarav Sharma)
--   Account 100000000002  PIN 7391   (Priya Patel)
-- ---------------------------------------------------------------------
INSERT INTO users (id, full_name, email, phone) VALUES
    (1, 'Aarav Sharma', 'aarav@example.com', '9876543210'),
    (2, 'Priya Patel',  'priya@example.com', '9123456780');

INSERT INTO accounts (account_number, user_id, pin_hash, pin_salt, balance) VALUES
    ('100000000001', 1, 'CTVOssJjUs4EwrlC2IJ+px+a+BhMKcXuoJy+0D2JmFw=', 'fFdVfFuy5wiR7c4aZM589g==', 50000.00),
    ('100000000002', 2, '/+pVMvcDEjRoUCjn4vrQEVzCsYm3DZd3NlxyFwN81Yw=', '4SBSqs4APHRfDHm0LSYwEw==', 23000.00);

INSERT INTO transactions (account_number, type, amount, balance_after, related_account, description, created_at) VALUES
    ('100000000001', 'DEPOSIT',      40000.00, 40000.00, NULL,           'Opening deposit',          NOW() - INTERVAL 10 DAY),
    ('100000000001', 'DEPOSIT',      15000.00, 55000.00, NULL,           'Cash deposit',             NOW() - INTERVAL 5 DAY),
    ('100000000001', 'TRANSFER_OUT',  5000.00, 50000.00, '100000000002', 'Transfer to Priya Patel',  NOW() - INTERVAL 2 DAY),
    ('100000000002', 'DEPOSIT',      20000.00, 20000.00, NULL,           'Opening deposit',          NOW() - INTERVAL 9 DAY),
    ('100000000002', 'TRANSFER_IN',   5000.00, 25000.00, '100000000001', 'Transfer from Aarav Sharma', NOW() - INTERVAL 2 DAY),
    ('100000000002', 'WITHDRAWAL',    2000.00, 23000.00, NULL,           'Cash withdrawal',          NOW() - INTERVAL 1 DAY);
