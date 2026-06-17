-- Agrega los permisos project:read y project:update que nunca fueron creados en V2
INSERT INTO permissions (name, description) VALUES
    ('project:read',   'Ver proyectos del catálogo y detalle'),
    ('project:update', 'Editar proyectos y cambiar su estado')
ON CONFLICT (name) DO NOTHING;

-- Asigna investment:read al rol ADMIN (fue creado en V7 pero solo se asignó a INVESTOR)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name = 'investment:read'
WHERE r.name = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Asigna project:read y project:update al rol ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('project:read', 'project:update')
WHERE r.name = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Asigna project:read y project:update al rol CREATOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('project:read', 'project:update')
WHERE r.name = 'CREATOR'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Asigna project:read al rol INVESTOR (puede navegar el catálogo autenticado)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name = 'project:read'
WHERE r.name = 'INVESTOR'
ON CONFLICT (role_id, permission_id) DO NOTHING;
