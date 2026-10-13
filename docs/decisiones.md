# Decisiones del proyecto (SIGA IPT)

Registro breve de decisiones tomadas durante el desarrollo. Actualizar a medida que avanza el proyecto.

## D01 - Base de datos: MySQL (no PostgreSQL)

- **Decisión:** usar MySQL 8.
- **Motivo:** ya esta configurado en el proyecto, coincide con la guia de entorno y con lo trabajado en clases.
- **Consecuencia:** los tests de integracion usaran el contenedor `mysql:8.0` de Testcontainers.

## D02 - Java 21

- **Decisión:** mantener Java 21 (LTS).
- **Motivo:** es el valor fijado en el `pom.xml` y es la version recomendada para Spring Boot.
- **Nota:** el proyecto usa **Spring Boot 4.1.1**. Si aparece un problema grave de compatibilidad
  con alguna libreria (springdoc, Testcontainers), la alternativa es bajar a Spring Boot 3.5.x.

## D03 - Libreria de UI del frontend: Material UI (MUI)

- **Decisión:** usar MUI.
- **Motivo:** coincide con el ejemplo trabajado en clases (`payroll-frontend`).
- **Alternativa aceptada por el enunciado:** Bootstrap.

## D04 - Seguridad primero (Keycloak + JWT)

- **Decisión:** implementar la seguridad en los primeros dias del proyecto, antes de las reglas de negocio.
- **Motivo:** es el componente de mayor riesgo e incertidumbre; construirlo temprano permite detectar
  problemas a tiempo y desarrollar el frontend ya autenticado (sin rehacerlo despues).

## D05 - Validacion del token: JWKS en lugar de solo issuer-uri

- **Decisión:** el backend valida el JWT usando `jwk-set-uri` (llaves publicas) y ademas verifica el emisor.
- **Motivo:** si se configura solo `issuer-uri`, Spring intenta contactar a Keycloak **al arrancar** y la
  aplicacion falla si Keycloak no esta disponible. Con `jwk-set-uri` las llaves se descargan la primera
  vez que llega un token, por lo que el backend arranca sin depender de Keycloak.
- **Ademas:** validar el emisor (`iss`) evita aceptar tokens de otros realms.

## D06 - Roles

- **Decisión:** roles de realm `ADMIN`, `TEACHER`, `STUDENT`.
- **Motivo:** son los actores definidos en el enunciado. El backend los recibe en `realm_access.roles`
  y los convierte a `ROLE_ADMIN`, etc. (ver `KeycloakRealmRoleConverter`).

## D07 - Regla de bloques horarios

- **Decisión:** la cantidad de bloques de una seccion es `TEL total / 2`
  (cada bloque corresponde a 2 horas pedagogicas).
- **Ejemplo:** TEL `2-2-2` = 6 horas -> 3 bloques.
- **Estado:** por confirmar con el docente; documentar aqui la interpretacion final.

## D08 - Puertos del entorno de desarrollo

| Servicio | Puerto |
|---|---|
| MySQL | 3306 |
| Keycloak | 8081 (dentro del contenedor 8080) |
| Backend Spring Boot | 8080 |
| Frontend Vite | 5173 |

Se usa 8081 para Keycloak en desarrollo para no chocar con el backend (8080).

## D09 - Cobertura de pruebas

- **Decisión:** la regla de JaCoCo que exige >= 90% de cobertura se activara cuando existan las clases
  de servicio (fase final). Por ahora JaCoCo solo genera el reporte.
- **Motivo:** no tiene sentido exigir cobertura de codigo que aun no existe.
