-- Agrega permiso investment:read y lo asigna a INVESTOR
INSERT INTO permissions (name, description) VALUES
    ('investment:read', 'Ver inversiones propias')
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'INVESTOR'
  AND p.name = 'investment:read'
ON CONFLICT (role_id, permission_id) DO NOTHING;
