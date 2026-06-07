-- Add AUDITOR role for HU-44: Auditoría y Validación de Proyectos
INSERT INTO roles (name, description) VALUES
    ('AUDITOR', 'Auditor que valida y aprueba proyectos para tokenización')
ON CONFLICT (name) DO NOTHING;

-- Add project:audit permission
INSERT INTO permissions (name, description) VALUES
    ('project:audit', 'Auditar y validar proyectos')
ON CONFLICT (name) DO NOTHING;

-- Assign project:audit to AUDITOR role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'AUDITOR'
  AND p.name = 'project:audit'
ON CONFLICT DO NOTHING;

-- ADMIN also gets project:audit (already has all permissions via wildcard, but explicit for clarity)
-- No explicit INSERT needed as ADMIN already has all permissions
