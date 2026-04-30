# Instructivo OAuth2 Google - Integración Frontend

## Arquitectura del Flujo

```
[Frontend] → [Backend /oauth2/authorization/google] → [Google Login] → [Backend callback] → [Frontend /oauth2/callback?token=JWT]
```

---

## Paso 1: Entender los Endpoints

| Acción | Endpoint | Descripción |
|--------|----------|-------------|
| Iniciar Login | `GET /oauth2/authorization/google` | Backend redirige a Google |
| Callback de Google | `GET /login/oauth2/code/google` | Manejado automáticamente por el backend |
| Retorno al Frontend | `GET /oauth2/callback?token=xxx` | Backend redirige acá con el JWT |

---

## Paso 2: Configuración de URL de Retorno

El backend redirige al frontend a:
```
http://localhost:5173/oauth2/callback
```

Para cambiarla (otro puerto u otro ambiente), setear la variable en el `.env` del backend:
```properties
APP_OAUTH2_REDIRECT_URI=http://localhost:3000/oauth2/callback
```

Si no se define, el default es `http://localhost:5173/oauth2/callback`.

---

## Paso 3: Configuración en Google Cloud Console (requerido)

En [console.cloud.google.com](https://console.cloud.google.com) → APIs & Services → Credentials → tu cliente OAuth2:

- **Authorized redirect URIs** debe tener:
  - Local: `http://localhost:8080/login/oauth2/code/google`
  - Producción: `https://tu-backend.com/login/oauth2/code/google`

Sin esto, Google rechaza el login con `invalid_client` o `redirect_uri_mismatch`.

---

## Paso 4: Botón "Login con Google"

```jsx
const handleGoogleLogin = () => {
  window.location.href = 'http://localhost:8080/oauth2/authorization/google';
};

// En el JSX:
<button onClick={handleGoogleLogin}>
  Iniciar sesión con Google
</button>
```

---

## Paso 5: Página de Callback

Crear un componente que escuche en `/oauth2/callback` y capture el token de la URL:

```jsx
// src/pages/OAuth2Callback.jsx
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

export default function OAuth2Callback() {
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const error = params.get('error');

    if (error) {
      navigate('/login?error=' + error);
      return;
    }

    if (!token) {
      navigate('/login?error=no_token');
      return;
    }

    // Guardar el token
    localStorage.setItem('jwt_token', token);

    // Opcional: decodificar el payload para obtener datos del usuario
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      // payload contiene: sub (email), userId, roles, permissions
    } catch (e) {}

    navigate('/dashboard');
  }, [navigate]);

  return <p>Procesando autenticación...</p>;
}
```

Registrar la ruta en el router:

```jsx
// src/App.jsx
import OAuth2Callback from './pages/OAuth2Callback';

<Route path="/oauth2/callback" element={<OAuth2Callback />} />
```

---

## Paso 6: Usar el JWT en los Requests

```javascript
const apiRequest = async (url, options = {}) => {
  const token = localStorage.getItem('jwt_token');

  const response = await fetch(`http://localhost:8080${url}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    },
  });

  if (response.status === 401) {
    localStorage.removeItem('jwt_token');
    window.location.href = '/login';
    return;
  }

  return response.json();
};
```

---

## Paso 7: Probar el Flujo Completo

1. Levantar el backend: `cd backend && ./mvnw spring-boot:run`
2. Levantar el frontend
3. Abrir en el navegador: `http://localhost:8080/oauth2/authorization/google`
   - (o hacer click en el botón del frontend)
4. Elegir cuenta de Google
5. El backend genera el JWT y redirige a `http://localhost:5173/oauth2/callback?token=xxx`
6. El componente `OAuth2Callback` guarda el token y redirige al dashboard

---

## Estructura del JWT

```json
{
  "sub": "usuario@gmail.com",
  "userId": 1,
  "roles": ["INVESTOR"],
  "permissions": ["READ_USER", "CREATE_PROJECT"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

---

## Checklist

- [ ] Botón que redirige a `http://localhost:8080/oauth2/authorization/google`
- [ ] Ruta `/oauth2/callback` registrada en el router
- [ ] Componente `OAuth2Callback` que lee `?token=` de la URL y lo guarda
- [ ] Header `Authorization: Bearer <token>` en todos los requests autenticados
- [ ] Logout: borrar el token del storage (`localStorage.removeItem('jwt_token')`)
- [ ] URI `http://localhost:8080/login/oauth2/code/google` registrada en Google Cloud Console

---

## Variables de entorno para el Frontend

```javascript
// Usar variable de entorno para no hardcodear la URL del backend
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
```

En `.env` del frontend:
```
VITE_API_URL=http://localhost:8080
```

Para producción, cambiar a la URL del backend deployado.

---

## Notas

- El usuario se crea automáticamente en la BD con rol `INVESTOR` si no existe.
- Si el usuario ya existe con ese email (creado por registro manual), el backend vincula el provider OAuth2 a esa cuenta.
- El token expira en 1 hora (configurable con `APP_SECURITY_JWT_EXPIRATION_MS` en el `.env` del backend).
