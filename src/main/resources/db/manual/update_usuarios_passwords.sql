-- BCrypt hashes generados para corregir autenticación desde tabla usuarios.
-- Contraseñas objetivo:
-- Dariel1234
-- Carlos1234

UPDATE usuarios
SET password = '$2b$12$T2dVTzjOY5/oKvIoO1MFVuM35lukIF6ti3EswPMbcso93doUDiCYS'
WHERE username = 'Dariel';

UPDATE usuarios
SET password = '$2b$12$Y14PvFxzIvJuzX9s0EqccuXjm9PL7nNagvyxtK0Vlr9NDGbb182sG'
WHERE username = 'Carlos';

-- Verificación rápida (debe mostrar ambos usuarios con hash tipo bcrypt que inicia con $2):
SELECT id, username, password, activo
FROM usuarios
WHERE username IN ('Dariel', 'Carlos');