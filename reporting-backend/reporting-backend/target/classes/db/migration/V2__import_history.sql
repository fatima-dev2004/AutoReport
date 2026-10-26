-- ============================================================================
-- V2 - Historique persistant des imports Excel (backend)
-- ============================================================================

CREATE TABLE import_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    user_id BIGINT,
    import_date DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    rows_total INT NOT NULL DEFAULT 0,
    rows_imported INT NOT NULL DEFAULT 0,
    rows_skipped INT NOT NULL DEFAULT 0,
    error_count INT NOT NULL DEFAULT 0,
    error_details TEXT,
    CONSTRAINT fk_import_history_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_import_history_date ON import_history(import_date);
