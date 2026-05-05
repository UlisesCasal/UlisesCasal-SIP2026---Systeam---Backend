# 🔐 Guía de Implementación: Refresh Token en el Frontend

## ⚠️ **LEÉ ESTO PRIMERO, HERMANO**

El backend **YA IMPLEMENTÓ** el sistema de Refresh Token. Si el frontend no lo usa, **el access token va a expirar y el usuario va a tener que volver a loguearse cada hora**. ¡Eso es UX terrible!

---

## 🎯 **OBJETIVO**

Cuando el `accessToken` expire (o esté por expirar), usar el `refreshToken` para obtener un **nuevo par de tokens** sin que el usuario se dé cuenta.

---

## 🔄 **FLUJO NUEVO (vs el anterior)**

### ANTES (Roto):
1. Login → Te daba UN solo token (`token`)
2. Ese token expiraba → Usuario tenía que loguearse de nuevo 😡

### AHORA (Arreglado):
1. Login → Te da **`accessToken`** + **`refreshToken`**
2. Usás el `accessToken` para las requests (expira en 1 hora)
3. Cuando el `accessToken` expira → Usás el `refreshToken` en `/auth/refresh`
4. El backend te da **NUEVOS** `accessToken` + `refreshToken` (rotación)
5. Si el `refreshToken` también expiró → A la mierda, hay que loguearse de nuevo

---

## 📂 **DÓNDE GUARDAR LOS TOKENS**

En tu frontend (sea React, Vue, Angular), guardalos en `localStorage` o `sessionStorage`:

```javascript
// Ejemplo en JavaScript vanilla / React
const ACCESS_TOKEN_KEY = 'systeam_access_token';
const REFRESH_TOKEN_KEY = 'systeam_refresh_token';

function saveTokens(accessToken, refreshToken) {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
}

function getAccessToken() {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
}

function getRefreshToken() {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
}

function clearTokens() {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
}
```

---

## 🔧 **CÓMO HACER UNA REQUEST AUTENTICADA**

Siempre mandá el `accessToken` en el header `Authorization`:

```javascript
async function fetchWithAuth(url, options = {}) {
    const token = getAccessToken();
    
    const headers = {
        ...options.headers,
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
    };

    return fetch(url, { ...options, headers });
}
```

---

## 🚨 **MANEJO DE 401 (LO MÁS IMPORTANTE)**

Cuando el backend responde con **401 Unauthorized**, significa que el `accessToken` expiró. **ACÁ ES DONDE ENTRÁ LA MAGIA**:

```javascript
async function fetchWithAutoRefresh(url, options = {}) {
    try {
        let response = await fetchWithAuth(url, options);

        // Si no es 401, devolvemos la response normal
        if (response.status !== 401) {
            return response;
        }

        // ⚠️ 401 detectado → Intentamos refrescar el token
        console.log('🔄 Access token expirado, refrescando...');
        
        const newTokens = await refreshAccessToken();
        
        if (!newTokens) {
            // Falló el refresh → El usuario tiene que loguearse de nuevo
            console.log('❌ Refresh token inválido, redirigiendo a login...');
            clearTokens();
            window.location.href = '/login'; // O tu ruta de login
            return;
        }

        // ✅ Refresh exitoso → Reintentamos la request original con el NUEVO token
        console.log('✅ Tokens refrescados, reintentando request...');
        return await fetchWithAuth(url, options);

    } catch (error) {
        console.error('Error en fetchWithAutoRefresh:', error);
        throw error;
    }
}

async function refreshAccessToken() {
    const refreshToken = getRefreshToken();
    
    if (!refreshToken) {
        return null; // No hay refresh token
    }

    try {
        const response = await fetch('http://localhost:8080/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: refreshToken })
        });

        if (!response.ok) {
            console.error('Refresh token inválido o expirado');
            clearTokens(); // Limpiamos todo
            return null;
        }

        const data = await response.json();
        
        // Guardamos los NUEVOS tokens (recordá que el backend rota los tokens)
        saveTokens(data.accessToken, data.refreshToken);
        
        return data;

    } catch (error) {
        console.error('Error refrescando token:', error);
        return null;
    }
}
```

---

## 🛠️ **IMPLEMENTACIÓN EN TU LOGIN (React Ejemplo)**

Si usás React, tu función de login debería quedar así:

```javascript
// services/authService.js
const API_URL = 'http://localhost:8080';

export async function login(email, password) {
    const response = await fetch(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
        throw new Error('Error en login');
    }

    const data = await response.json();
    
    // ⚠️ OJO: Ahora vienen DOS tokens
    saveTokens(data.accessToken, data.refreshToken);
    
    return data; // Devolvemos toda la info (userId, roles, etc.)
}
```

---

## 🔐 **CAMBIOS EN EL FLUJO DE GOOGLE OAUTH2**

¡**ESTO CAMBIÓ, HERMANO!** Antes el callback venía así:
```
http://localhost:5173/oauth2/callback?token=UN_SOLO_TOKEN
```

Ahora viene así:
```
http://localhost:5173/oauth2/callback?accessToken=XXX&refreshToken=YYY
```

### Cómo leer el nuevo callback en React (Ejemplo):

```javascript
// components/OAuth2Callback.jsx
import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

export function OAuth2Callback() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {
        // ⚠️ OJO: Ahora son DOS parámetros
        const accessToken = searchParams.get('accessToken');
        const refreshToken = searchParams.get('refreshToken');
        const error = searchParams.get('error');

        if (error) {
            console.error('Error en OAuth2:', error);
            navigate('/login?error=' + error);
            return;
        }

        if (!accessToken || !refreshToken) {
            console.error('No se recibieron los tokens');
            navigate('/login?error=no_tokens');
            return;
        }

        // Guardamos AMBOS tokens
        saveTokens(accessToken, refreshToken);
        navigate('/dashboard'); // O donde quieras redirigir
    }, [searchParams, navigate]);

    return <div>Cargando...</div>;
}
```

---

## 📋 **CHECKLIST PARA VOS (HERMANO)**

Antes de decir "está listo", verificá que:

- [ ] **Login tradicional** (`/auth/login`) → Guarda `accessToken` Y `refreshToken`
- [ ] **Callback de Google** (`/oauth2/callback`) → Lee `?accessToken=` y `&refreshToken=`
- [ ] **Requests autenticadas** → Usan `Authorization: Bearer <accessToken>`
- [ ] **Interceptor de 401** → Si dan 401, usan `/auth/refresh` automáticamente
- [ ] **Si refresh falla** → Borran tokens y redirigen a `/login`
- [ ] **Logout** → Borran AMBOS tokens del storage

---

## 🧪 **CÓMO PROBAR QUE FUNCIONA**

1. Logueate normal
2. Abrí la consola del navegador → `Application` → `Local Storage`
3. Fijate que tengas los dos tokens guardados
4. **Hacé que el access token expire** (podés cambiar la expiración en el backend a 1 minuto para testear rápido)
5. Hacé una request a una ruta protegida
6. **Si funciona**: Vas a ver en la consola "🔄 Access token expirado, refrescando..." y luego "✅ Tokens refrescados"
7. **Mirá el Local Storage de nuevo**: Los tokens deberían haber cambiado (rotación)

---

## ❓ **PREGUNTAS FRECUENTES**

### ¿Cuánto tiempo vive cada token?
- **Access Token**: 1 hora (configurable en `APP_SECURITY_JWT_EXPIRATION_MS`)
- **Refresh Token**: 7 días (configurable en `APP_SECURITY_JWT_REFRESH_EXPIRATION_MS`)

### ¿El refresh token se puede usar múltiples veces?
**NO**. Cada vez que usás el refresh token:
1. El viejo se **revoca** (no sirve más)
2. El backend te da un **par nuevo**

Esto se llama **Rotación de Refresh Tokens** y es MÁS SEGURO porque si alguien roba un refresh token viejo, no sirve.

### ¿Qué pasa si el usuario tiene dos pestañas abiertas?
Podrías tener un problema de "race condition" donde ambas pestañas intentan refrescar el token al mismo tiempo. Para producción robusta, deberías usar un **refresh token queue** o `mutex`, pero para empezar, la implementación de arriba funciona bien.

---

## 📱 **EJEMPLO COMPLETO (React + Axios)**

Si usás Axios en lugar de `fetch`, acá tenés un interceptor:

```javascript
// services/api.js
import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080',
    headers: { 'Content-Type': 'application/json' }
});

// Interceptor para agregar el token a TODAS las requests
api.interceptors.request.use(config => {
    const token = getAccessToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Interceptor para manejar 401 y refrescar automáticamente
api.interceptors.response.use(
    response => response,
    async error => {
        const originalRequest = error.config;

        // Si es 401 y no es el endpoint de refresh (para evitar bucle infinito)
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            const newTokens = await refreshAccessToken();
            
            if (!newTokens) {
                // Redirect a login
                window.location.href = '/login';
                return Promise.reject(error);
            }

            // Reintentamos la request original con el nuevo token
            originalRequest.headers.Authorization = `Bearer ${newTokens.accessToken}`;
            return api(originalRequest);
        }

        return Promise.reject(error);
    }
);

export default api;
```

---

## 🎉 **LISTO, HERMANO**

Si implementás esto tal cual, tu app va a tener una experiencia de usuario **PROFESIONAL**. El usuario no va a saber que los tokens se están refrescando en segundo plano.

**¡PONETE LAS PILAS Y HACÉLO BIEN!** 🚀
