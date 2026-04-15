# Systeam Backend (RBAC + JWT)

Backend del proyecto con Spring Boot, autenticación JWT y autorización basada en permisos (RBAC).

## Requisitos

- Java 17+
- Maven Wrapper (incluido)
- Una base de datos PostgreSQL (Supabase, Docker, o local)

## Variables de entorno

El repo incluye un archivo de ejemplo: `.env.example`.

1. Copiar:

```bash
cp .env.example .env
```

2. Completar las variables en `.env`:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:PUERTO/DATABASE
SPRING_DATASOURCE_USERNAME=tu_usuario
SPRING_DATASOURCE_PASSWORD=tu_password
APP_SECURITY_JWT_SECRET=tu_secret_muy_largo_y_unico
APP_SECURITY_JWT_EXPIRATION_MS=3600000
```

> **Nota:** `.env` está ignorado por git. Nunca subirlo al repositorio.

---

## Opción A: Supabase (Producción/Cloud)

### Datos de conexión de Supabase

1. Ir a **Supabase Dashboard** → tu proyecto → **Settings** → **Database**

2. Usar los datos de connection pooling (PgBouncer):

| Variable | Valor |
|----------|-------|
| `HOST` | `aws-1-sa-east-1.pooler.supabase.com` |
| `PUERTO` | `6543` (¡no 5432!) |
| `DATABASE` | `postgres` |
| `USERNAME` | `postgres.xxxxx` (tu connection string user) |
| `PASSWORD` | Tu password de base de datos |

3. Configurar en `.env`:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:6543/postgres
SPRING_DATASOURCE_USERNAME=postgres.xxxxx
SPRING_DATASOURCE_PASSWORD=tu_password
```

### Verificar conexión

```bash
cd backend
./mvnw spring-boot:run
```

Deberías ver: `Started BackendApplication in X seconds`

---

## Opción B: Docker Compose (Local)

```bash
docker compose up --build
```

Servicios:
- API: `http://localhost:8080`
- Postgres: `localhost:5432`

Para apagar:

```bash
docker compose down
```

Para apagar y borrar volumen de DB:

```bash
docker compose down -v
```

---

## Opción C: Postgres local

1. Tener PostgreSQL corriendo localmente
2. Crear base de datos `postgres`
3. Configurar `.env` con:
   - `HOST`: `localhost`
   - `PUERTO`: `5432`
   - `DATABASE`: `postgres`
   - `USERNAME`: `postgres`
   - `PASSWORD`: tu_password

---

## Compilar y testear

Desde `backend`:

```bash
./mvnw -DskipTests clean compile
./mvnw test
```

---

## Iniciar la aplicación

```bash
cd backend
./mvnw spring-boot:run
```

---

## Credenciales de prueba

### Admin por defecto

| Campo | Valor |
|-------|-------|
| Email | `admin@ideafy.local` |
| Password | `password` |
| Rol | `ADMIN` |

> ⚠️ **Importante:** El password del admin está hasheado con BCrypt. Si tenés problemas de login, ver la sección de [Troubleshooting](#troubleshooting).

---

## Guía de testing rápido

### 1) Login admin

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@ideafy.local","password":"password"}'
```

Respuesta esperada:
```json
{
  "accessToken": "eyJhbG...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "userId": 1,
  "email": "admin@ideafy.local",
  "roles": ["ADMIN"],
  "permissions": [...]
}
```

### 2) Registrar usuario

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "User Demo",
    "email": "user.demo@ideafy.local",
    "password": "password123"
  }'
```

### 3) Ver perfil propio (autenticado)

```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer TU_ACCESS_TOKEN"
```

### 4) Probar permisos

- Sin token a `/api/users` → `401 Unauthorized`
- Usuario sin permiso a endpoint admin → `403 Forbidden`
- Admin con token válido → `200` o `201`

---

## Endpoints principales

### Auth

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/login` | Login con email/password |
| POST | `/auth/register` | Registrar nuevo usuario |
| POST | `/auth/change-password` | Cambiar password (autenticado) |

### Usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/users/me` | Perfil del usuario autenticado |
| GET | `/api/users` | Listar usuarios (admin) |
| GET | `/api/users/{id}` | Detalle de usuario (admin) |
| POST | `/api/users` | Crear usuario (admin) |
| PUT | `/api/users/{id}` | Editar usuario (admin) |
| DELETE | `/api/users/{id}` | Eliminar usuario (admin) |
| POST | `/api/users/{userId}/roles/{roleId}` | Asignar rol |
| DELETE | `/api/users/{userId}/roles/{roleId}` | Quitar rol |

### Roles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/roles` | Listar roles |
| GET | `/api/roles/{id}` | Detalle de rol |
| POST | `/api/roles` | Crear rol (admin) |
| PUT | `/api/roles/{id}` | Editar rol (admin) |
| DELETE | `/api/roles/{id}` | Eliminar rol (admin) |
| POST | `/api/roles/{roleId}/permissions/{permissionId}` | Asignar permiso |
| DELETE | `/api/roles/{roleId}/permissions/{permissionId}` | Quitar permiso |

### Permisos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/permissions` | Listar permisos |
| GET | `/api/permissions/{id}` | Detalle de permiso |
| POST | `/api/permissions` | Crear permiso (admin) |
| PUT | `/api/permissions/{id}` | Editar permiso (admin) |
| DELETE | `/api/permissions/{id}` | Eliminar permiso (admin) |

---

## Troubleshooting

### Error: `400 Bad Credentials` en login

**Causa:** El hash BCrypt del password en la base de datos no coincide con el password que estás enviando.

**Solución:**

1. Generar un hash BCrypt correcto para tu password. Podés usar [BCrypt Generator](https://bcrypt-generator.com/) o ejecutar en Python:

```python
import bcrypt
hash_correcto = bcrypt.hashpw(b'tu_password', bcrypt.gensalt(rounds=10)).decode()
print(hash_correcto)
```

2. Actualizar la base de datos:

```sql
UPDATE users 
SET password = 'tu_hash_aqui' 
WHERE email = 'admin@ideafy.local';
```

3. Verificar el cambio:

```sql
SELECT password FROM users WHERE email = 'admin@ideafy.local';
```

### Error: `Migration checksum mismatch`

**Causa:** Flyway detecta que un archivo de migración fue modificado después de ejecutarse.

**Solución:**

```bash
./mvnw flyway:repair \
  -Dflyway.url="jdbc:postgresql://HOST:PUERTO/DATABASE" \
  -Dflyway.user="USUARIO" \
  -Dflyway.password="PASSWORD"
```

O ejecutar en la base de datos:

```sql
DELETE FROM flyway_schema_history WHERE version = 'NUMERO_DE_VERSION';
```

Y luego `./mvnw spring-boot:run` para que Flyway re-ejecute la migración.

### Error: `Unable to connect to the database`

**Causa:** Credenciales incorrectas o base de datos no accessible.

**Solución:**

1. Verificar que la base de datos esté corriendo
2. Revisar las variables `SPRING_DATASOURCE_*` en `.env`
3. Si usás Supabase, verificar que el **puerto sea 6543** (no 5432)

### Error: `prepared statement "S_1" already exists`

**Causa:** Conflicto de PgBouncer (Supabase) con Flyway.

**Solución:** Agregar en `application.properties`:

```properties
spring.flyway.connect-retries=0
```

Este error no impide que la app funcione.

### Error: `401 Unauthorized` con token válido

**Causa:** El header `Authorization` no tiene el formato correcto.

**Solución:** Verificar que el header sea exactamente:

```
Authorization: Bearer TU_TOKEN_AQUI
```

Con un espacio entre `Bearer` y el token, sin comillas adicionales.

---

## Troubleshooting rápido

- **Error de conexión a DB:** Revisar `SPRING_DATASOURCE_*`
- **Error JWT:** Revisar `APP_SECURITY_JWT_SECRET`
- **`401` con token:** Verificar formato `Bearer <token>`
- **Checksum mismatch:** Ejecutar `flyway:repair`
- **Login falla:** Regenerar hash BCrypt y actualizar base de datos
