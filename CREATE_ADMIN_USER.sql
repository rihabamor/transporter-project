-- =====================================================
-- CREATE ADMIN USER FOR TRANSPORTEUR PLATFORM
-- Run this SQL script on your MySQL database (port 3307)
-- Database: transporteur_db
-- =====================================================

USE transporteur_db;

-- Create admin account
-- Email: admin@transporteur.com
-- Password: Admin@123 (hashed with BCrypt)
-- BCrypt hash for 'Admin@123': $2a$10$YourHashHere (you need to generate this)

-- Note: You need to generate the BCrypt hash for the password.
-- You can use an online BCrypt generator or the Spring Boot application

-- Option 1: Using a placeholder password (CHANGE THIS!)
-- You should update the password hash after creating the account
INSERT INTO compte (email, password, role, date_creation) 
VALUES (
    'admin@transporteur.com',
    '$2a$10$rOg3L7IvLKzJvQfKLX1QN.pXd8F7qGFKqZqZqGFKqZqZqGFKqZqZq',  -- PLACEHOLDER! Update this!
    'ADMIN',
    NOW()
);

-- =====================================================
-- IMPORTANT: Update the password hash!
-- =====================================================
-- To generate a proper BCrypt hash:
-- 1. Run your Spring Boot application
-- 2. Use the register endpoint with email: admin@transporteur.com
-- 3. Then manually update the role to ADMIN:
--    UPDATE compte SET role = 'ADMIN' WHERE email = 'admin@transporteur.com';
--
-- OR use this Java code to generate BCrypt hash:
-- String password = "Admin@123";
-- String hash = BCryptPasswordEncoder().encode(password);
-- System.out.println(hash);
-- =====================================================

-- Verify admin account created
SELECT id, email, role, date_creation 
FROM compte 
WHERE role = 'ADMIN';

-- =====================================================
-- ALTERNATIVE METHOD: Create via existing account
-- =====================================================
-- If you have an existing account you want to promote to admin:
-- UPDATE compte SET role = 'ADMIN' WHERE email = 'your_email@example.com';
-- =====================================================
