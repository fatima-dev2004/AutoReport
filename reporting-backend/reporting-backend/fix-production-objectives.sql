-- ================================================
-- FIX: Ajouter la colonne max_defects manquante
-- ================================================

USE reporting_db;

-- Vérifier si la colonne existe déjà
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'reporting_db' 
  AND TABLE_NAME = 'production_objectives' 
  AND COLUMN_NAME = 'max_defects';

-- Ajouter la colonne si elle n'existe pas
ALTER TABLE production_objectives 
ADD COLUMN IF NOT EXISTS max_defects INT NOT NULL DEFAULT 0 
AFTER target_quality_rate;

-- Vérifier que la colonne a été ajoutée
DESCRIBE production_objectives;

-- Message de confirmation
SELECT 'Colonne max_defects ajoutée avec succès!' AS Status;
