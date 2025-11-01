-- Script de creación de Base de Datos para Nissum Technical Challenge
-- Este script es generado automáticamente por Hibernate, pero se documenta aquí para referencia

-- Crear tabla de usuarios
CREATE TABLE users (
    id BINARY(16) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL,
    modified TIMESTAMP NOT NULL,
    last_login TIMESTAMP,
    token TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Crear tabla de teléfonos
CREATE TABLE phones (
    id BINARY(16) NOT NULL PRIMARY KEY,
    number VARCHAR(255) NOT NULL,
    city_code VARCHAR(255) NOT NULL,
    country_code VARCHAR(255) NOT NULL,
    user_id BINARY(16) NOT NULL,
    CONSTRAINT fk_phones_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Índices para optimizar consultas
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_phones_user_id ON phones(user_id);

-- Comentarios sobre las tablas
COMMENT ON TABLE users IS 'Tabla principal de usuarios del sistema';
COMMENT ON TABLE phones IS 'Tabla de teléfonos asociados a usuarios';

-- Comentarios sobre columnas importantes
COMMENT ON COLUMN users.id IS 'Identificador único UUID del usuario';
COMMENT ON COLUMN users.email IS 'Email único del usuario, usado para login';
COMMENT ON COLUMN users.password IS 'Contraseña encriptada con BCrypt';
COMMENT ON COLUMN users.token IS 'Token JWT válido para el usuario';
COMMENT ON COLUMN users.is_active IS 'Indica si el usuario está activo en el sistema';
COMMENT ON COLUMN users.created IS 'Fecha y hora de creación del usuario';
COMMENT ON COLUMN users.modified IS 'Fecha y hora de última modificación';
COMMENT ON COLUMN users.last_login IS 'Fecha y hora del último acceso del usuario';

-- Datos de ejemplo para testing (opcional)
-- INSERT INTO users (id, name, email, password, created, modified, last_login, token, is_active) 
-- VALUES (
--     RANDOM_UUID(),
--     'Juan Rodriguez',
--     'juan@rodriguez.org',
--     '$2a$10$...',  -- BCrypt hash de 'Hunter2@123'
--     CURRENT_TIMESTAMP,
--     CURRENT_TIMESTAMP,
--     CURRENT_TIMESTAMP,
--     'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
--     TRUE
-- );