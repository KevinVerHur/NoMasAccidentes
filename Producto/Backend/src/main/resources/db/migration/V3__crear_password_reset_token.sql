-- =====================================================
-- V3: tabla para recuperación de contraseña
-- =====================================================

CREATE TABLE password_reset_token (
    id_token     BIGINT AUTO_INCREMENT PRIMARY KEY,
    token        VARCHAR(36)  NOT NULL UNIQUE,
    id_usuario   BIGINT       NOT NULL,
    expira_en    DATETIME     NOT NULL,
    usado        BOOLEAN      NOT NULL DEFAULT FALSE,
    creado_en    DATETIME     NOT NULL,
    CONSTRAINT fk_prt_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_prt_token ON password_reset_token(token);
