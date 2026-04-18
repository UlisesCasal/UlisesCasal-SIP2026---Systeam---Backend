# API Systeam Backend - Documentación para Frontend

## Tabla de Contenidos
1. [Información General](#información-general)
2. [Autenticación](#autenticación)
3. [Endpoints de Usuarios](#endpoints-de-usuarios)
4. [Endpoints de Roles](#endpoints-de-roles)
5. [Endpoints de Permisos](#endpoints-de-permisos)
6. [Códigos de Error](#códigos-de-error)
7. [Ejemplos de Uso](#ejemplos-de-uso)

---

## Información General

**Base URL:** `http://localhost:8080`

**Tipo de autenticación:** JWT (JSON Web Token)

**Encabezados requeridos:** Para endpoints protegidos, incluir el token JWT en el header:
```
Authorization: Bearer <token_jwt>
```

---

## Autenticación

### 1. Login
Iniciar sesión y obtener token JWT.

- **Método:** `POST`
- **Endpoint:** `/auth/login`
- **Requiere autenticación:** No
- **Permisos necesarios:** Ninguno

#### Request Body
```json
{
  "email": "string (requerido, formato email válido)",
  "password": "string (requerido)"
}
```

#### Response (200 OK)
```json
{
  "accessToken": "string (token JWT para usar en header Authorization)",
  "tokenType": "Bearer",
  "expiresIn": 3600 (tiempo en segundos),
  "userId": 1,
  "email": "usuario@ejemplo.com",
  "roles": ["ADMIN", "USER"],
  "permissions": ["user:read", "user:create", ...]
}
```

---

### 2. Register
Registrar un nuevo usuario.

- **Método:** `POST`
- **Endpoint:** `/auth/register`
- **Requiere autenticación:** No
- **Permisos necesarios:** Ninguno

#### Request Body
```json
{
  "name": "string (requerido)",
  "email": "string (requerido, formato email válido)",
  "password": "string (requerido, mínimo 8 caracteres)"
}
```

#### Response (201 Created)
```json
{
  "id": 1,
  "name": "Juan Perez",
  "email": "juan@ejemplo.com",
  "enabled": true,
  "roles": ["USER"],
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### 3. Change Password
Cambiar la contraseña del usuario actual.

- **Método:** `POST`
- **Endpoint:** `/auth/change-password`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** Estar autenticado

#### Request Body
```json
{
  "currentPassword": "string (requerido)",
  "newPassword": "string (requerido, mínimo 8 caracteres)"
}
```

#### Response (200 OK)
Sin cuerpo de respuesta (éxito).

---

## Endpoints de Usuarios

### 1. Obtener usuario actual
Obtiene los datos del usuario actualmente autenticado.

- **Método:** `GET`
- **Endpoint:** `/api/users/me`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** Estar autenticado

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "Juan Perez",
  "email": "juan@ejemplo.com",
  "enabled": true,
  "roles": ["USER"],
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### 2. Obtener todos los usuarios
Lista todos los usuarios con paginación.

- **Método:** `GET`
- **Endpoint:** `/api/users?page=0&size=10`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `user:read`

#### Query Parameters
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| page | int | 0 | Número de página (0-indexed) |
| size | int | 10 | Cantidad de elementos por página |

#### Response (200 OK)
```json
{
  "content": [
    {
      "id": 1,
      "name": "Juan Perez",
      "email": "juan@ejemplo.com",
      "enabled": true,
      "roles": ["USER"],
      "createdAt": "2024-01-15T10:30:00"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "size": 10,
  "number": 0
}
```

---

### 3. Obtener usuario por ID
Obtiene un usuario específico por su ID.

- **Método:** `GET`
- **Endpoint:** `/api/users/{id}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `user:read`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | Long | ID del usuario |

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "Juan Perez",
  "email": "juan@ejemplo.com",
  "enabled": true,
  "roles": ["USER"],
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### 4. Crear usuario
Crea un nuevo usuario en el sistema.

- **Método:** `POST`
- **Endpoint:** `/api/users`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `user:create`

#### Request Body
```json
{
  "name": "string (requerido)",
  "email": "string (requerido, formato email válido)",
  "password": "string (requerido, mínimo 8 caracteres)"
}
```

#### Response (201 Created)
```json
{
  "id": 1,
  "name": "Juan Perez",
  "email": "juan@ejemplo.com",
  "enabled": true,
  "roles": ["USER"],
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### 5. Actualizar usuario
Actualiza los datos de un usuario existente.

- **Método:** `PUT`
- **Endpoint:** `/api/users/{id}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `user:update`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | Long | ID del usuario a actualizar |

#### Request Body
```json
{
  "name": "string (requerido)",
  "email": "string (requerido, formato email válido)"
}
```

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "Juan Perez Actualizado",
  "email": "juan@ejemplo.com",
  "enabled": true,
  "roles": ["USER"],
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### 6. Desactivar usuario
Desactiva (elimina lógicamente) un usuario.

- **Método:** `DELETE`
- **Endpoint:** `/api/users/{id}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `user:delete`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | Long | ID del usuario a desactivar |

#### Response (204 No Content)
Sin cuerpo de respuesta.

---

### 7. Asignar rol a usuario
Asigna un rol a un usuario específico.

- **Método:** `POST`
- **Endpoint:** `/api/users/{userId}/roles/{roleId}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `user:update`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| userId | Long | ID del usuario |
| roleId | Long | ID del rol a asignar |

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "Juan Perez",
  "email": "juan@ejemplo.com",
  "enabled": true,
  "roles": ["USER", "ADMIN"],
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### 8. Revocar rol de usuario
Revoca un rol de un usuario específico.

- **Método:** `DELETE`
- **Endpoint:** `/api/users/{userId}/roles/{roleId}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `user:update`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| userId | Long | ID del usuario |
| roleId | Long | ID del rol a revocar |

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "Juan Perez",
  "email": "juan@ejemplo.com",
  "enabled": true,
  "roles": ["USER"],
  "createdAt": "2024-01-15T10:30:00"
}
```

---

## Endpoints de Roles

### 1. Obtener todos los roles
Lista todos los roles del sistema.

- **Método:** `GET`
- **Endpoint:** `/api/roles`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `role:read`

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "name": "ADMIN",
    "description": "Rol de administrador",
    "permissions": ["user:read", "user:create", "user:update", "user:delete", ...]
  },
  {
    "id": 2,
    "name": "USER",
    "description": "Rol de usuario estándar",
    "permissions": ["user:read"]
  }
]
```

---

### 2. Obtener rol por ID
Obtiene un rol específico por su ID.

- **Método:** `GET`
- **Endpoint:** `/api/roles/{id}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `role:read`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | Long | ID del rol |

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "ADMIN",
  "description": "Rol de administrador",
  "permissions": ["user:read", "user:create", "user:update", "user:delete", ...]
}
```

---

### 3. Crear rol
Crea un nuevo rol en el sistema.

- **Método:** `POST`
- **Endpoint:** `/api/roles`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `role:create`

#### Request Body
```json
{
  "name": "string (requerido)",
  "description": "string (opcional)"
}
```

#### Response (201 Created)
```json
{
  "id": 1,
  "name": "MODERATOR",
  "description": "Rol de moderador",
  "permissions": []
}
```

---

### 4. Actualizar rol
Actualiza un rol existente.

- **Método:** `PUT`
- **Endpoint:** `/api/roles/{id}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `role:update`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | Long | ID del rol a actualizar |

#### Request Body
```json
{
  "name": "string (requerido)",
  "description": "string (opcional)"
}
```

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "ADMIN_ACTUALIZADO",
  "description": "Descripción actualizada",
  "permissions": ["user:read", "user:create", ...]
}
```

---

### 5. Eliminar rol
Elimina un rol del sistema.

- **Método:** `DELETE`
- **Endpoint:** `/api/roles/{id}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `role:delete`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | Long | ID del rol a eliminar |

#### Response (204 No Content)
Sin cuerpo de respuesta.

---

### 6. Asignar permiso a rol
Asigna un permiso a un rol específico.

- **Método:** `POST`
- **Endpoint:** `/api/roles/{roleId}/permissions/{permissionId}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `role:update`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| roleId | Long | ID del rol |
| permissionId | Long | ID del permiso a asignar |

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "ADMIN",
  "description": "Rol de administrador",
  "permissions": ["user:read", "user:create", "permission:read", ...]
}
```

---

### 7. Revocar permiso de rol
Revoca un permiso de un rol específico.

- **Método:** `DELETE`
- **Endpoint:** `/api/roles/{roleId}/permissions/{permissionId}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `role:update`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| roleId | Long | ID del rol |
| permissionId | Long | ID del permiso a revocar |

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "ADMIN",
  "description": "Rol de administrador",
  "permissions": ["user:read", "user:create", ...]
}
```

---

## Endpoints de Permisos

### 1. Obtener todos los permisos
Lista todos los permisos del sistema.

- **Método:** `GET`
- **Endpoint:** `/api/permissions`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `permission:read`

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "name": "user:read",
    "description": "Permiso para leer usuarios"
  },
  {
    "id": 2,
    "name": "user:create",
    "description": "Permiso para crear usuarios"
  }
]
```

---

### 2. Obtener permiso por ID
Obtiene un permiso específico por su ID.

- **Método:** `GET`
- **Endpoint:** `/api/permissions/{id}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `permission:read`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | Long | ID del permiso |

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "user:read",
  "description": "Permiso para leer usuarios"
}
```

---

### 3. Crear permiso
Crea un nuevo permiso en el sistema.

- **Método:** `POST`
- **Endpoint:** `/api/permissions`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `permission:create`

#### Request Body
```json
{
  "name": "string (requerido)",
  "description": "string (opcional)"
}
```

#### Response (201 Created)
```json
{
  "id": 1,
  "name": "report:generate",
  "description": "Permiso para generar reportes"
}
```

---

### 4. Actualizar permiso
Actualiza un permiso existente.

- **Método:** `PUT`
- **Endpoint:** `/api/permissions/{id}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `permission:update`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | Long | ID del permiso a actualizar |

#### Request Body
```json
{
  "name": "string (requerido)",
  "description": "string (opcional)"
}
```

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "user:read_actualizado",
  "description": "Descripción actualizada"
}
```

---

### 5. Eliminar permiso
Elimina un permiso del sistema.

- **Método:** `DELETE`
- **Endpoint:** `/api/permissions/{id}`
- **Requiere autenticación:** Sí
- **Permisos necesarios:** `permission:delete`

#### Path Parameters
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | Long | ID del permiso a eliminar |

#### Response (204 No Content)
Sin cuerpo de respuesta.

---

## Códigos de Error

| Código | Descripción |
|--------|-------------|
| 200 | OK - Solicitud exitosa |
| 201 | Created - Recurso creado exitosamente |
| 204 | No Content - Solicitud exitosa sin contenido |
| 400 | Bad Request - Error en la solicitud (validación) |
| 401 | Unauthorized - No autenticado o token inválido |
| 403 | Forbidden - No tiene permisos suficientes |
| 404 | Not Found - Recurso no encontrado |
| 409 | Conflict - Conflicto de datos (ej: email duplicado) |
| 500 | Internal Server Error - Error del servidor |

---

## Ejemplos de Uso

### Flujo de autenticación típico

1. **Login** - Obtener token JWT
```javascript
// Request
fetch('http://localhost:8080/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'admin@systeam.com',
    password: 'password123'
  })
})
.then(res => res.json())
.then(data => {
  // Guardar token para próximas solicitudes
  const token = data.accessToken;
});

// Response
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 1,
  "email": "admin@systeam.com",
  "roles": ["ADMIN"],
  "permissions": ["user:read", "user:create", "user:update", "user:delete"]
}
```

2. **Usar el token en solicitudes protegidas**
```javascript
const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...';

fetch('http://localhost:8080/api/users', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
.then(res => res.json())
.then(data => console.log(data));
```

---

### Ejemplo: Crear usuario

```javascript
const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...';

fetch('http://localhost:8080/api/users', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name: 'Nuevo Usuario',
    email: 'nuevo@ejemplo.com',
    password: 'password123'
  })
})
.then(res => res.json())
.then(data => console.log(data));
```

---

### Ejemplo: Asignar rol a usuario

```javascript
const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...';
const userId = 1;
const roleId = 2;

fetch(`http://localhost:8080/api/users/${userId}/roles/${roleId}`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
.then(res => res.json())
.then(data => console.log(data));
```

---

### Ejemplo: Paginación de usuarios

```javascript
const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...';

fetch('http://localhost:8080/api/users?page=0&size=20', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
.then(res => res.json())
.then(data => {
  console.log('Usuarios:', data.content);
  console.log('Total elementos:', data.totalElements);
  console.log('Total páginas:', data.totalPages);
});
```

---

## Notas Importantes

1. **El token JWT debe incluirse en cada request** (excepto login/register) con el header `Authorization: Bearer <token>`

2. **Los permisos siguen el patrón** `recurso:operación` (ej: `user:read`, `user:create`, `role:update`)

3. **La paginación es 0-indexed** - la primera página es `page=0`

4. **Los passwords requieren mínimo 8 caracteres**

5. **Los emails deben ser únicos en el sistema** - intentar registrar un email existente retorna error 409

6. **Los roles y permisos se asignan jerárquicamente** - los roles agrupan permisos y se asignan a usuarios