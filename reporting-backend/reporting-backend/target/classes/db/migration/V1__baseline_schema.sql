-- ============================================================================
-- V1 - Schema de base (baseline)
--
-- Genere manuellement a partir des entites JPA du projet (src/main/java/.../entity)
-- pour remplacer spring.jpa.hibernate.ddl-auto=update par des migrations Flyway
-- versionnees.
--
-- IMPORTANT AVANT DE L'UTILISER EN PRODUCTION :
-- Ce script n'a pas pu etre valide par une execution reelle (pas de MySQL/Maven
-- disponibles dans l'environnement ou il a ete redige). Avant de l'appliquer sur
-- une base de production :
--   1) Sur une base de DEV vide, demarrer une fois l'appli avec
--      spring.jpa.hibernate.ddl-auto=update (profil par defaut) pour laisser
--      Hibernate creer le schema.
--   2) Comparer ce schema genere avec le present fichier :
--        mysqldump --no-data -u root reporting_db > schema_hibernate.sql
--      et resoudre les eventuels ecarts (types, longueurs, index).
--   3) Une fois valide, activer Flyway (voir application.properties) et repasser
--      ddl-auto sur "validate" partout, y compris en dev.
-- ============================================================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    profile_photo_url VARCHAR(500),
    department VARCHAR(255),
    phone_number VARCHAR(255),
    position VARCHAR(255),
    created_at DATETIME(6),
    last_login DATETIME(6),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_permissions (
    user_id BIGINT NOT NULL,
    permission VARCHAR(60) NOT NULL,
    CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    type VARCHAR(30),
    period_start DATE,
    period_end DATE,
    total_production DOUBLE,
    total_defects INT,
    quality_rate DOUBLE,
    production_by_line VARCHAR(2000),
    department VARCHAR(255),
    pdf_file_path VARCHAR(255),
    excel_file_path VARCHAR(255),
    generated_by BIGINT,
    generated_at DATETIME(6),
    source_file_name VARCHAR(255),
    CONSTRAINT fk_reports_generated_by FOREIGN KEY (generated_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(255) NOT NULL,
    action_type VARCHAR(40) NOT NULL,
    timestamp DATETIME(6) NOT NULL,
    details TEXT,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE report_authorizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    authorized_by_user_id BIGINT,
    authorized_at DATETIME(6),
    authorization_reason VARCHAR(500),
    CONSTRAINT uk_report_auth_report_user UNIQUE (report_id, user_id),
    CONSTRAINT fk_report_auth_report FOREIGN KEY (report_id) REFERENCES reports(id),
    CONSTRAINT fk_report_auth_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_report_auth_authorized_by FOREIGN KEY (authorized_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(255) NOT NULL,
    setting_value TEXT,
    setting_category VARCHAR(255),
    setting_description VARCHAR(255),
    updated_at DATETIME(6),
    updated_by BIGINT,
    CONSTRAINT uk_system_settings_key UNIQUE (setting_key),
    CONSTRAINT fk_system_settings_updated_by FOREIGN KEY (updated_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE report_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type VARCHAR(30),
    cron_expression VARCHAR(255),
    recipients VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE production_objectives (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    end_date DATE NOT NULL,
    line VARCHAR(255) NOT NULL,
    max_defects INT NOT NULL DEFAULT 0,
    notes VARCHAR(255),
    period_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    target_quality_rate DOUBLE NOT NULL,
    target_quantity INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE production_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    production_date DATE NOT NULL,
    production_line VARCHAR(255) NOT NULL,
    quantity DOUBLE NOT NULL,
    defects INT NOT NULL,
    source_file_name VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE email_recipients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_reports_period ON reports(period_start, period_end);
CREATE INDEX idx_production_records_date_line ON production_records(production_date, production_line);
