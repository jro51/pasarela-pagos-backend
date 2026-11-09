-- Normaliza los roles al formato sin prefijo ROLE_ (USER, ADMIN, ...).
UPDATE users
SET role = SUBSTRING(role, 6)
WHERE role LIKE 'ROLE_%';

-- El valor por defecto tambien debe usar el formato sin prefijo.
ALTER TABLE users
    MODIFY COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER';
