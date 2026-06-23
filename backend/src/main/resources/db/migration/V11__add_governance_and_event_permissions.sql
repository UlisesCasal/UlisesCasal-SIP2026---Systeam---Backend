-- Agrega permisos de gobernanza y eventos, asignados a INVESTOR, CREATOR y ADMIN
-- Los endpoints existen en el backend pero los permisos nunca fueron creados en la DB.

INSERT INTO permissions (name, description) VALUES
    ('governance:read',    'Ver propuestas, detalle y contador de gobernanza'),
    ('governance:vote',    'Votar en propuestas de gobernanza'),
    ('governance:create',  'Crear nuevas propuestas'),
    ('governance:execute', 'Ejecutar propuestas aprobadas'),
    ('evento:read',        'Ver eventos y su detalle')
ON CONFLICT (name) DO NOTHING;

-- Asigna todos los permisos de gobernanza + evento:read a INVESTOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'INVESTOR'
  AND p.name IN ('governance:read', 'governance:vote', 'governance:create', 'governance:execute', 'evento:read')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Asigna governance:read y evento:read a CREATOR (puede ver propuestas y eventos)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'CREATOR'
  AND p.name IN ('governance:read', 'evento:read')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Asigna los nuevos permisos a ADMIN (V2 le dio todos los que existían en ese momento,
-- pero los nuevos hay que asignarlos explícitamente)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN ('governance:read', 'governance:vote', 'governance:create', 'governance:execute', 'evento:read')
ON CONFLICT (role_id, permission_id) DO NOTHING;
