-- ============================================================================
-- DATABASE MIGRATION: Price Update Feature
-- ============================================================================
-- Description: Add price_history table to track all price changes for missions
-- Date: December 1, 2025
-- Version: 1.0
-- ============================================================================

-- Create price_history table
CREATE TABLE IF NOT EXISTS price_history (
    id_price_history BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key for price history',
    mission_id BIGINT NOT NULL COMMENT 'Foreign key to mission table',
    old_price DOUBLE NULL COMMENT 'Previous price (NULL for initial proposal)',
    new_price DOUBLE NOT NULL COMMENT 'New price value',
    change_reason VARCHAR(500) DEFAULT 'Modification du prix par le transporteur' COMMENT 'Reason for price change',
    changed_by VARCHAR(255) NOT NULL COMMENT 'Email of the transporteur who made the change',
    change_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp of the price change',
    
    -- Foreign key constraint
    CONSTRAINT fk_price_history_mission 
        FOREIGN KEY (mission_id) 
        REFERENCES mission(id_mission) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tracks all price changes for missions';

-- Create indexes for performance optimization
CREATE INDEX idx_price_history_mission_id 
    ON price_history(mission_id)
    COMMENT 'Index for fast lookups by mission ID';

CREATE INDEX idx_price_history_change_date 
    ON price_history(change_date DESC)
    COMMENT 'Index for sorting by change date';

CREATE INDEX idx_price_history_changed_by 
    ON price_history(changed_by)
    COMMENT 'Index for filtering by transporteur';

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Verify table creation
SELECT 
    TABLE_NAME,
    ENGINE,
    TABLE_ROWS,
    CREATE_TIME
FROM 
    information_schema.TABLES
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'price_history';

-- Verify indexes
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    INDEX_TYPE
FROM 
    information_schema.STATISTICS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'price_history'
ORDER BY 
    INDEX_NAME, SEQ_IN_INDEX;

-- Verify foreign key constraint
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM 
    information_schema.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'price_history'
    AND REFERENCED_TABLE_NAME IS NOT NULL;

-- ============================================================================
-- SAMPLE DATA (Optional - for testing purposes)
-- ============================================================================

-- Uncomment the following lines to insert sample price history data
-- Replace mission_id with actual mission IDs from your database

/*
-- Example: Initial price proposal
INSERT INTO price_history (mission_id, old_price, new_price, change_reason, changed_by, change_date)
VALUES (1, NULL, 100.00, 'Initial price proposal', 'transporteur@example.com', NOW() - INTERVAL 2 DAY);

-- Example: First price update
INSERT INTO price_history (mission_id, old_price, new_price, change_reason, changed_by, change_date)
VALUES (1, 100.00, 120.00, 'Adjusted for fuel costs', 'transporteur@example.com', NOW() - INTERVAL 1 DAY);

-- Example: Second price update
INSERT INTO price_history (mission_id, old_price, new_price, change_reason, changed_by, change_date)
VALUES (1, 120.00, 150.00, 'Distance recalculée - route plus longue', 'transporteur@example.com', NOW());
*/

-- ============================================================================
-- ROLLBACK SCRIPT (In case you need to undo this migration)
-- ============================================================================

/*
-- CAUTION: This will delete all price history data!
-- Uncomment and run only if you need to rollback this migration

-- Drop indexes
DROP INDEX IF EXISTS idx_price_history_mission_id ON price_history;
DROP INDEX IF EXISTS idx_price_history_change_date ON price_history;
DROP INDEX IF EXISTS idx_price_history_changed_by ON price_history;

-- Drop table
DROP TABLE IF EXISTS price_history;
*/

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
-- The price_history table is now ready to track all price changes!
-- Backend application will automatically populate this table when
-- transporteurs update prices via PUT /api/missions/{id}/update-price
-- ============================================================================
