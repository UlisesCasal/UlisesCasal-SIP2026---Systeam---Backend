# Instructivo OAuth2 Google - Integración Frontend

## Arquitectura del Flujo

```
[Frontend] → [Backend] → [Google] → [Backend] → [Frontend con JWT]
```

---

## Paso 1: Entender los Endpoints

| Acción | Endpoint Backend | Descripción |
|--------|------------------|-------------|
| **Iniciar Login** | `GET /oauth2/authorization/google` | Redirige a Google para autenticación |
| **Callback de Google** | `GET /login/oauth2/code/google` | Google redirige acá (manejado por backend) |
| **Retorno al Frontend** | `GET /oauth2/callback?token=xxx` | Backend redirige con el JWT acá |

---

## Paso 2: Configuración de URL de Retorno

El backend está configurado para redirigir al frontend en:
```
http://localhost:5173/oauth2/callback
```

**Si tu frontend corre en otro puerto** (ej. 3000), avisale al backend dev para cambiar la configuración en `application.properties`:
```properties
app.oauth2.redirect-uri=${APP_OAUTH2_REDIRECT_URI:http://localhost:5173/oauth2/callback}
```

---

## Paso 3: Implementar el Botón "Login con Google"

### Opción A: Redirect Simple (Recomendado para empezar)

```javascript
// En tu componente de Login
const handleGoogleLogin = () => {
  // Redirigir al backend para iniciar flujo OAuth2
  window.location.href = 'http://localhost:8080/oauth2/authorization/google';
};
```

```html
<button onClick={handleGoogleLogin}>
  Iniciar sesión con Google
</button>
```

---

## Paso 4: Manejar el Callback en el Frontend

Crear una página/componente que escuche en `/oauth2/callback`:

### React Example:

```jsx
// src/pages/OAuth2Callback.jsx
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

export default function OAuth2Callback() {
  const navigate = useNavigate();

  useEffect(() => {
    // Extraer el token de la URL
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const error = params.get('error');

    if (error) {
      console.error('Error en OAuth2:', error);
      navigate('/login?error=' + error);
      return;
    }

    if (!token) {
      console.error('No se recibió el token');
      navigate('/login?error=no_token');
      return;
    }

    // Guardar el token (localStorage, sessionStorage, o cookies seguras)
    localStorage.setItem('jwt_token', token);

    // Opcional: Decodificar el payload del JWT para obtener info del usuario
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      console.log('Usuario:', payload.sub);
      console.log('Roles:', payload.roles);
      console.log('Permissions:', payload.permissions);
      console.log('userId:', payload.userId);
    } catch (e) {
      console.warn('No se pudo decodificar el token');
    }

    // Redirigir al dashboard o home
    navigate('/dashboard');
  }, [navigate]);

  return (
    <div>
      <p>Procesando autenticación...</p>
    </div>
  );
}
```

### Configurar la ruta en React Router:

```jsx
// src/App.jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import OAuth2Callback from './pages/OAuth2Callback';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/oauth2/callback" element={<OAuth2Callback />} />
        {/* Otras rutas */}
      </Routes>
    </BrowserRouter>
  );
}
```

---

## Paso 5: Usar el JWT en las Requests

Para hacer requests autenticadas al backend, agregar el token en el header:

```javascript
// Función helper para requests autenticadas
const apiRequest = async (url, options = {}) => {
  const token = localStorage.getItem('jwt_token');
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  const response = await fetch(`http://localhost:8080${url}`, {
    ...options,
    headers,
  });
  
  if (response.status === 401) {
    // Token expirado o inválido
    localStorage.removeItem('jwt_token');
    window.location.href = '/login';
    return;
  }
  
  return response.json();
};

// Ejemplo de uso
const getUserData = async () => {
  const data = await apiRequest('/api/users/me');
  return data;
};
```

---

## Paso 6: Verificar que el Backend está Corriendo

Asegurate de que el backend esté corriendo en `http://localhost:8080` antes de probar.

---

## Paso 7: Probar el Flujo Completo

1. Iniciar el backend: `cd backend && ./mvnw spring-boot:run`
2. Iniciar el frontend
3. Hacer click en "Login con Google"
4. Deberías ser redirigido a Google
5. Autenticarte con tu cuenta
6. Volver al frontend en `/oauth2/callback?token=xxx`
7. El token se guarda y podés usarlo para requests autenticadas ✅

---

## Checklist para el Frontend Dev

- [ ] Botón "Login con Google" que redirige a `http://localhost:8080/oauth2/authorization/google`
- [ ] Página que escuche en `/oauth2/callback` para capturar el token
- [ ] Guardar el JWT en `localStorage`/`sessionStorage`/`httpOnly cookie`
- [ ] Agregar header `Authorization: Bearer <token>` en las requests
- [ ] Manejar errores (`?error=token_generation_failed`)
- [ ] Logout: simplemente borrar el token del storage

---

## Estructura de respuesta del JWT

El backend genera un JWT con este payload:

```json
{
  "sub": "user@email.com",
  "userId": 123,
  "roles": ["INVESTOR"],
  "permissions": ["READ_USER", "CREATE_PROJECT"],
  "iat": 1234567890,
  "exp": 1234567890
}
```

---

## Notas para Producción

Cuando el backend haga deploy, la URL base cambiará de `http://localhost:8080` a la URL de producción. El frontend debería leer la URL base de una variable de entorno:

```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
```

Y usar `API_BASE_URL` en lugar de hardcodear la URL.

---

## Contacto

Si hay algún problema con el backend OAuth2, contactar al backend dev.
