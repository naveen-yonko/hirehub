-- Sample user data
-- Password is 'password123' encoded with BCrypt
INSERT INTO users (id, email, password, role) VALUES 
('1', 'test@example.com', '$2a$10$ZOSqdkLSNspRNGmzIEbJ8uDkr3NOnAsRniADFkSEWlcPD4V0rUSCO', 'customer');