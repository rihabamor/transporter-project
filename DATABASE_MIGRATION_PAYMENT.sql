-- =====================================================
-- DATABASE MIGRATION FOR PAYMENT SYSTEM
-- Run this SQL script on your MySQL database (port 3307)
-- Database: transporteur_db
-- =====================================================

USE transporteur_db;

-- Step 1: Increase the size of 'statut' column to accommodate new status values
-- Old statuses: EN_ATTENTE (10), ACCEPTEE (8), EN_COURS (8), TERMINEE (8), ANNULEE (7)
-- New statuses: PRIX_PROPOSE (13), PRIX_CONFIRME (13)
-- Setting to VARCHAR(20) for safety
ALTER TABLE mission 
MODIFY COLUMN statut VARCHAR(20) NOT NULL;

-- Step 2: Add payment-related columns to mission table (if not already added)
-- These columns may already exist if JPA auto-created them
ALTER TABLE mission 
ADD COLUMN IF NOT EXISTS proposed_price DOUBLE DEFAULT NULL,
ADD COLUMN IF NOT EXISTS price_confirmed BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS is_paid BOOLEAN DEFAULT FALSE;

-- Step 3: Create payment table
CREATE TABLE IF NOT EXISTS payment (
    id_payment BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_id BIGINT NOT NULL UNIQUE,  -- One payment per mission
    client_id BIGINT NOT NULL,
    transporteur_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    card_last_four VARCHAR(4),
    card_holder_name VARCHAR(255),
    payment_date DATETIME NOT NULL,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    
    -- Foreign keys
    CONSTRAINT fk_payment_mission FOREIGN KEY (mission_id) REFERENCES mission(id_mission),
    CONSTRAINT fk_payment_client FOREIGN KEY (client_id) REFERENCES client(id_client),
    CONSTRAINT fk_payment_transporteur FOREIGN KEY (transporteur_id) REFERENCES transporteur(id_transporteur),
    
    -- Indexes for performance
    INDEX idx_payment_mission (mission_id),
    INDEX idx_payment_client (client_id),
    INDEX idx_payment_transporteur (transporteur_id),
    INDEX idx_payment_transaction (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- Verification Queries
-- =====================================================

-- Check mission table structure
DESCRIBE mission;

-- Check payment table structure
DESCRIBE payment;

-- Check if any existing missions need status update
SELECT id_mission, statut, proposed_price, price_confirmed, is_paid 
FROM mission 
LIMIT 10;

-- =====================================================
-- INSTRUCTIONS:
-- =====================================================
-- 1. Connect to MySQL: mysql -u root -p -P 3307
-- 2. Run this script: source D:/_5edma/rihebwchayma/back/DATABASE_MIGRATION_PAYMENT.sql
-- 3. Verify changes with DESCRIBE commands above
-- 4. Restart your Spring Boot application
-- =====================================================
