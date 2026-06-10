-- Migración para alinear la base de datos con el modelo de clases Java actualizado
-- Agrega campos de auditoría (deleted_at, updated_at) y fecha_nacimiento

-- 1. Agregar las columnas a la tabla users
-- Para la fecha de nacimiento, como ya hay usuarios y debe ser NOT NULL, 
-- primero agregamos la columna permitiendo nulos o con un valor por defecto.
ALTER TABLE users ADD COLUMN fecha_nacimiento DATE;

-- Asignamos una fecha por defecto a los usuarios existentes (ej: 1900-01-01) para que no rompa la base de datos
UPDATE users SET fecha_nacimiento = '1900-01-01' WHERE fecha_nacimiento IS NULL;

-- Ahora sí, marcamos la columna como NOT NULL para los nuevos usuarios
ALTER TABLE users ALTER COLUMN fecha_nacimiento SET NOT NULL;

-- Agregamos la columna de borrado lógico
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;


-- 2. Agregar columnas faltantes a la tabla roles
ALTER TABLE roles ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();
ALTER TABLE roles ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;

-- Opcionalmente, podemos quitar el default después si queremos que se maneje solo desde la aplicación
ALTER TABLE roles ALTER COLUMN updated_at DROP DEFAULT;
UPDATE roles SET updated_at = created_at WHERE updated_at IS NULL;


-- 3. Agregar columnas faltantes a la tabla permissions
ALTER TABLE permissions ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();
ALTER TABLE permissions ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE permissions ALTER COLUMN updated_at DROP DEFAULT;
UPDATE permissions SET updated_at = created_at WHERE updated_at IS NULL;
