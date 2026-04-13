-- Roles de IDEAFY
INSERT INTO roles (name, description) VALUES
    ('ADMIN',    'Administrador de la plataforma'),
    ('CREATOR',  'Emprendedor que publica proyectos'),
    ('INVESTOR', 'Usuario que invierte en proyectos');

-- Permisos
INSERT INTO permissions (name, description) VALUES
    ('user:create',       'Crear usuarios'),
    ('user:read',         'Ver usuarios'),
    ('user:update',       'Editar usuarios'),
    ('user:delete',       'Desactivar usuarios'),
    ('role:create',       'Crear roles'),
    ('role:read',         'Ver roles'),
    ('role:update',       'Editar roles'),
    ('role:delete',       'Eliminar roles'),
    ('permission:read',   'Ver permisos'),
    ('project:create',    'Publicar proyectos'),
    ('investment:create', 'Invertir en proyectos');

-- ADMIN tiene todos los permisos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- CREATOR puede publicar proyectos y ver usuarios
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'CREATOR'
  AND p.name IN ('project:create', 'user:read');

-- INVESTOR puede invertir y ver proyectos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'INVESTOR'
  AND p.name IN ('investment:create', 'user:read');