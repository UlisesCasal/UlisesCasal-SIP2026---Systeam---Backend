# Systeam Backend (RBAC + JWT)

Backend del proyecto con Spring Boot, autenticacion JWT y autorizacion basada en permisos (RBAC).

## Requisitos

- Java 17+
- Maven Wrapper (incluido)
- Docker + Docker Compose (opcional, recomendado)

## Variables de entorno

El repo incluye un archivo de ejemplo: `.env.example`.

1. Copiar:

```bash
cp .env.example .env
```

2. Verificar/completar variables en `.env`:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_SECURITY_JWT_SECRET`
- `APP_SECURITY_JWT_EXPIRATION_MS`

Nota: `.env` esta ignorado por git.

## Opcion 1: correr con Docker (recomendado)

Desde la raiz del repo:

```bash
docker compose up --build
```

Servicios:

- API: `http://localhost:8080`
- Postgres (contenedor): `localhost:5432`

Para apagar:

```bash
docker compose down
```

Para apagar y borrar volumen de DB:

```bash
docker compose down -v
```

## Opcion 2: correr local (sin Docker)

1. Tener Postgres accesible con las credenciales configuradas.
2. Ir a `backend`.
3. Levantar app:

```bash
cd backend
./mvnw spring-boot:run
```

## Compilar y testear

Desde `backend`:

```bash
./mvnw -DskipTests clean compile
./mvnw test
```

## Flujo rapido para probar seguridad

### 1) Login admin

Endpoint:

- `POST /auth/login`

Body:

```json
{
  "email": "admin@ideafy.local",
  "password": "password"
}
```

Respuesta esperada:

- `200 OK` con `accessToken`.

### 2) Usar token Bearer

Header:

```text
Authorization: Bearer <accessToken>
```

### 3) Probar permisos

- Sin token a `/api/users` -> `401`
- Usuario sin permiso a endpoint admin -> `403`
- Admin con token valido -> `200/201` segun endpoint

## Endpoints principales

### Auth

- `POST /auth/login`
- `POST /auth/register`
- `POST /auth/change-password` (autenticado)

### Usuarios

- `GET /api/users/me`
- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`
- `POST /api/users/{userId}/roles/{roleId}`
- `DELETE /api/users/{userId}/roles/{roleId}`

### Roles

- `GET /api/roles`
- `GET /api/roles/{id}`
- `POST /api/roles`
- `PUT /api/roles/{id}`
- `DELETE /api/roles/{id}`
- `POST /api/roles/{roleId}/permissions/{permissionId}`
- `DELETE /api/roles/{roleId}/permissions/{permissionId}`

### Permisos

- `GET /api/permissions`
- `GET /api/permissions/{id}`
- `POST /api/permissions`
- `PUT /api/permissions/{id}`
- `DELETE /api/permissions/{id}`

## Troubleshooting rapido

- Error de conexion a DB: revisar `SPRING_DATASOURCE_*`.
- Error JWT: revisar `APP_SECURITY_JWT_SECRET`.
- `401` con token: verificar formato `Bearer <token>`.
- Si cambias migraciones y tenes datos viejos de Docker, ejecutar `docker compose down -v` y volver a levantar.
