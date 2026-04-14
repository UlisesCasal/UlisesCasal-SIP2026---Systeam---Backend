INSERT INTO users (name, email, password, enabled)
VALUES (
    'Admin IDEAFY',
    'admin@ideafy.local',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi1M7.6Xj1k2xYDs2fzGEylh4G6dkhW',
    TRUE
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.email = 'admin@ideafy.local'
ON CONFLICT (user_id, role_id) DO NOTHING;
