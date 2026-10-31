# SIGA IPT

Sistema de Gestión Académica para el Taller de Ingeniería de Software.

## Stack

- Backend: Spring Boot 4 / Java 21 / Spring Data JPA / Spring Security OAuth2 Resource Server.
- Frontend: React 19 / Vite / MUI / Axios / React Router.
- Seguridad: Keycloak 26 con JWT y roles `ADMIN`, `TEACHER`, `STUDENT`.
- Base de datos: MySQL 8.
- Producción: Docker Compose + Nginx + 3 réplicas del backend.

## Usuarios de demostración

El login se realiza con **correo electrónico**:

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@siga.local` | `admin123` |
| Docente | `teacher@siga.local` | `teacher123` |
| Estudiante | `student@siga.local` | `student123` |

> La consola de administración de Keycloak usa `admin` / `admin` en el realm `master`.

## Desarrollo local

```bash
# 1. MySQL y Keycloak
docker compose -f docker-compose.dev.yml up -d

# 2. Backend
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 3. Frontend
cd frontend
npm install
npm run dev
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Keycloak: http://localhost:8081
- Swagger: http://localhost:8080/swagger-ui.html

## Pruebas

```bash
cd backend
./mvnw verify
```

Incluye:

- pruebas unitarias de services,
- 18 pruebas de integración con H2 + MockMvc,
- JaCoCo con mínimo 90% de cobertura de líneas en `services`.

El frontend se verifica con:

```bash
cd frontend
npm run lint
npm run build
```

## Docker de producción

### 1. Configurar variables

```bash
cp .env.example .env
```

Editar `.env`:

- contraseñas de MySQL,
- contraseña del administrador de Keycloak,
- `PUBLIC_APP_URL` (URL pública del frontend),
- `KEYCLOAK_PUBLIC_URL` (URL pública de Keycloak),
- secreto de `siga-admin`,
- imágenes de Docker Hub.

### 2. Construir/descargar imágenes

Las imágenes se publican en Docker Hub mediante GitHub Actions:

```text
dkrii/siga-backend:latest
dkrii/siga-frontend:latest
```

También se pueden construir localmente:

```bash
docker build -t dkrii/siga-backend:latest ./backend
docker build -t dkrii/siga-frontend:latest \
  --build-arg VITE_KEYCLOAK_URL=https://auth.tudominio.cl \
  --build-arg VITE_KEYCLOAK_REALM=siga \
  --build-arg VITE_KEYCLOAK_CLIENT_ID=siga-frontend \
  ./frontend
```

> Las variables `VITE_*` se incrustan en el build. Si cambia la URL de Keycloak, se debe reconstruir el frontend.

> Para un dominio real también hay que agregar la URL pública en Keycloak:
> `Clients → siga-frontend → Valid redirect URIs` y `Web origins`.
> El realm de ejemplo permite `http://localhost` y `http://localhost:5173` para pruebas locales.

### 3. Levantar producción

```bash
docker compose -f docker-compose.yml up -d
```

Servicios:

- `siga-prod-mysql`
- `siga-prod-keycloak`
- `siga-prod-backend1`, `backend2`, `backend3`
- `siga-prod-frontend`
- `siga-prod-nginx`

Solo Nginx y Keycloak quedan expuestos. MySQL y los backends están en la red interna.

### 4. Verificar

```bash
docker compose -f docker-compose.yml ps
curl http://localhost/health
curl http://localhost/api/public/ping
```

### 5. Detener

```bash
docker compose -f docker-compose.yml down
```

Para eliminar también la base de datos de producción:

```bash
docker compose -f docker-compose.yml down -v
```

## CI/CD

El workflow `.github/workflows/ci.yml` ejecuta:

1. `./mvnw verify` del backend.
2. `npm ci`, `npm run lint` y `npm run build` del frontend.
3. Si ambos pasan y el push es a `main`, construye y publica:
   - `dkrii/siga-backend:latest`
   - `dkrii/siga-frontend:latest`

### Secretos/variables de GitHub

Secret:

```text
DOCKERHUB_TOKEN
```

Repository variable:

```text
VITE_KEYCLOAK_URL=https://auth.tudominio.cl
```

## Estructura

```text
backend/            API Spring Boot
frontend/           Aplicación React
keycloak/           Realm, tema y usuarios de Keycloak
mysql/init/         Creación de bases de datos de producción
nginx/              Balanceador y entrada HTTP
docker-compose.yml  Stack de producción
docker-compose.dev.yml  Infraestructura de desarrollo
```
