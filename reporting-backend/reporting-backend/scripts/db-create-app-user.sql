-- ============================================================================
-- Creation d'un utilisateur MySQL dedie pour l'application en production,
-- avec des droits limites (pas de root, pas de DROP/GRANT).
--
-- A executer UNE FOIS par un DBA/admin, connecte en root sur le serveur MySQL
-- de production. Remplacer 'CHANGER_MOI' par un mot de passe fort genere
-- (ex: openssl rand -base64 32), puis stocker ce mot de passe uniquement dans
-- la variable d'environnement DB_PASSWORD (jamais dans un fichier commite).
-- ============================================================================

CREATE DATABASE IF NOT EXISTS reporting_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'reporting_app'@'%' IDENTIFIED BY 'CHANGER_MOI';

-- Droits necessaires a l'application au quotidien : lecture/ecriture des donnees
-- + creation/modification de tables (necessaire pour que Flyway applique les
-- migrations). Pas de droits d'administration (pas de GRANT, DROP DATABASE, etc.)
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
    ON reporting_db.* TO 'reporting_app'@'%';

-- Table de suivi Flyway
GRANT CREATE, DROP ON reporting_db.flyway_schema_history TO 'reporting_app'@'%';

FLUSH PRIVILEGES;

-- Verification :
--   SHOW GRANTS FOR 'reporting_app'@'%';
