CREATE TABLE IF NOT EXISTS totp_recovery_tokens (
    recovery_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    recovery_method VARCHAR(20) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    attempt_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (recovery_id),
    CONSTRAINT chk_totp_recovery_method
        CHECK (recovery_method IN ('EMAIL', 'PHONE', 'SECURITY_CODE')),
    CONSTRAINT fk_totp_recovery_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    INDEX idx_totp_recovery_lookup
        (user_id, recovery_method, used, created_at)
);
