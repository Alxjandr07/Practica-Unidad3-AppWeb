-- V2: Usuario administrador inicial para pruebas de autenticacion (JWT + cookie HttpOnly)
-- password en texto plano: Admin#2026  (hash BCrypt generado con strength 10)
INSERT INTO usuarios (username, password_hash, nombre_completo, email, rol, activo)
VALUES (
    'admin',
    '$2b$10$QVXmPcfM1UovR6lmTmBJNe.z0EaUnihH7juW7tgTdAak4ceTkFTf2',
    'Administrador Cooperativa',
    'admin@cooperativa-transporte.ec',
    'ADMIN',
    TRUE
);
