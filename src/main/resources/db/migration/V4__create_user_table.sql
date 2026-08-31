-- Create app_user table
CREATE TABLE app_user (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed default admin account
-- username: admin
-- password: admin-secure-pwd-2026
-- Hashed using BCrypt strength 10
INSERT INTO app_user (username, password, role)
VALUES ('admin', '$2a$10$re3.BQQCLJ0QET0tOWvO0OThxi/94nD8uqYvhg/Pa2gDLGgZTnRZG', 'ADMIN');
