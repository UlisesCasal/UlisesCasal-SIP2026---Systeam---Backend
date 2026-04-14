# Validacion Del Flujo RBAC (Checklist)

Este documento resume que probar para validar el flujo completo de autenticacion + autorizacion por permisos (RBAC).

## Precondiciones

- Backend levantado (`docker compose up --build` o local con Maven).
- Base de datos migrada con Flyway.
- Usuario admin disponible:
  - `email`: `admin@ideafy.local`
  - `password`: `password`
- Herramienta para pruebas API (Postman, Insomnia o curl).

## Variables sugeridas (Postman)

- `baseUrl` = `http://localhost:8080`
- `adminToken` = (vacio al inicio)
- `userToken` = (vacio al inicio)
- `createdUserId` = (vacio al inicio)
- `createdRoleId` = (vacio al inicio)
- `createdPermissionId` = (vacio al inicio)

## 1) Salud Basica

- `GET {{baseUrl}}/api/users` sin token
  - Esperado: `401 Unauthorized`
- Confirmar mensaje claro de error (`No autenticado` o equivalente).

## 2) Autenticacion

### 2.1 Login admin valido

- `POST {{baseUrl}}/auth/login`
- Body:

```json
{
  "email": "admin@ideafy.local",
  "password": "password"
}
```

- Esperado:
  - `200 OK`
  - respuesta con `accessToken`
- Guardar `accessToken` en `adminToken`.

### 2.2 Login invalido

- Mismo endpoint con password incorrecta.
- Esperado: `401` o error de credenciales invalido.

## 3) Registro Y Perfil Propio

### 3.1 Registro usuario estandar

- `POST {{baseUrl}}/auth/register`
- Body ejemplo:

```json
{
  "name": "User Demo",
  "email": "user.demo@ideafy.local",
  "password": "password123"
}
```

- Esperado:
  - `201 Created`
  - rol por defecto asignado (normalmente `INVESTOR`)
- Guardar `id` en `createdUserId`.

### 3.2 Login del usuario nuevo

- `POST {{baseUrl}}/auth/login` con ese email/password.
- Esperado: `200` + token.
- Guardar en `userToken`.

### 3.3 Perfil propio

- `GET {{baseUrl}}/api/users/me` con `Authorization: Bearer {{userToken}}`
- Esperado: `200` y datos del mismo usuario autenticado.

## 4) Autorizacion (401 vs 403)

### 4.1 Endpoint admin sin token

- `GET {{baseUrl}}/api/roles`
- Esperado: `401`.

### 4.2 Endpoint admin con usuario sin permisos

- `GET {{baseUrl}}/api/roles` con `userToken`.
- Esperado: `403 Forbidden`.

### 4.3 Endpoint admin con adminToken

- `GET {{baseUrl}}/api/roles` con `adminToken`.
- Esperado: `200`.

## 5) ABM De Permisos

### 5.1 Crear permiso

- `POST {{baseUrl}}/api/permissions` con `adminToken`
- Body:

```json
{
  "name": "demo:run",
  "description": "Permiso de prueba para validacion"
}
```

- Esperado: `200/201`.
- Guardar id en `createdPermissionId`.

### 5.2 Listar y detalle

- `GET /api/permissions` -> `200`
- `GET /api/permissions/{id}` -> `200`

### 5.3 Editar permiso

- `PUT /api/permissions/{id}` -> `200`

### 5.4 Eliminar permiso

- `DELETE /api/permissions/{id}` -> `204`

## 6) ABM De Roles

### 6.1 Crear rol

- `POST {{baseUrl}}/api/roles` con `adminToken`
- Body:

```json
{
  "name": "QA_ROLE",
  "description": "Rol temporal para validacion"
}
```

- Esperado: `200/201`.
- Guardar id en `createdRoleId`.

### 6.2 Listar y detalle

- `GET /api/roles` -> `200`
- `GET /api/roles/{id}` -> `200`

### 6.3 Editar rol

- `PUT /api/roles/{id}` -> `200`

## 7) Asignaciones RBAC

### 7.1 Asignar permiso a rol

- `POST /api/roles/{createdRoleId}/permissions/{createdPermissionId}` con admin.
- Esperado: `200` y rol con permiso asociado.

### 7.2 Revocar permiso de rol

- `DELETE /api/roles/{createdRoleId}/permissions/{createdPermissionId}`.
- Esperado: `200`.

### 7.3 Asignar rol a usuario

- `POST /api/users/{createdUserId}/roles/{createdRoleId}`.
- Esperado: `200`.

### 7.4 Revocar rol de usuario

- `DELETE /api/users/{createdUserId}/roles/{createdRoleId}`.
- Esperado: `200`.

## 8) Modulo Usuarios

### 8.1 Listado paginado

- `GET /api/users?page=0&size=10` con admin.
- Esperado: `200` y estructura paginada.

### 8.2 Detalle

- `GET /api/users/{createdUserId}` con admin.
- Esperado: `200`.

### 8.3 Edicion

- `PUT /api/users/{createdUserId}` con admin.
- Esperado: `200`.

### 8.4 Baja/Desactivacion

- `DELETE /api/users/{createdUserId}` con admin.
- Esperado: `204`.

## 9) Integridad De Roles

- Intentar borrar un rol que este asignado a algun usuario.
- `DELETE /api/roles/{idAsignado}` con admin.
- Esperado: error de negocio (no permitir borrado).

## 10) Cambio De Password

- `POST /auth/change-password` con token valido.
- Body:

```json
{
  "currentPassword": "password123",
  "newPassword": "newPassword123"
}
```

- Esperado:
  - `200`/`204` segun implementacion.
  - login con password vieja falla.
  - login con password nueva funciona.

## 11) Criterios De Aceptacion Final

Marcar como OK solo si se cumple todo:

- [ ] Login con email/password contra DB.
- [ ] JWT emitido y usable en `Bearer`.
- [ ] Hash de password (no texto plano).
- [ ] `401` sin token o token invalido.
- [ ] `403` cuando falta permiso.
- [ ] ABM de usuarios funcionando.
- [ ] ABM de roles funcionando.
- [ ] ABM de permisos funcionando.
- [ ] Asignar/revocar permisos a roles.
- [ ] Asignar/revocar roles a usuarios.
- [ ] Integridad al borrar roles asignados.
- [ ] Al menos 2 roles diferenciados en uso real.

## 12) Evidencia Recomendada Para Demo

- Capturas o export de requests/responses (Postman).
- Un mini guion de 10-15 requests en orden:
  - login admin
  - 401 sin token
  - 403 user sin permiso
  - crear rol/permiso
  - asignaciones
  - endpoint protegido exitoso con admin
  - cambio de password
