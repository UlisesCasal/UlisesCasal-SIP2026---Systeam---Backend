INSERT INTO permissions (name, description) VALUES
    ('permission:create', 'Crear permisos'),
    ('permission:update', 'Editar permisos'),
    ('permission:delete', 'Eliminar permisos')
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON 1=1
WHERE r.name = 'ADMIN'
  AND p.name IN ('permission:create', 'permission:update', 'permission:delete')
ON CONFLICT (role_id, permission_id) DO NOTHING;
