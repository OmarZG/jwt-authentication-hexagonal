```markdown
# jwt-authentication-hexagonal

Proyecto de ejemplo: autenticación con JWT (RSA) + refresh tokens persistentes usando Arquitectura Hexagonal.

Paquete base: org.zgo.auth

Requisitos
- JDK 21
- Maven 3.8+ (o la versión que prefieras)
- Git

Resumen
- JWT firmado con RS256 (RSA private/public key).
- Refresh tokens persistentes en BD (H2 por defecto).
- Rotación de refresh token al refrescar (el token antiguo se marca como revoked).
- Endpoint para revocar refresh tokens manualmente.
- Roles: ROLE_USER, ROLE_ADMIN.
- OpenAPI / Swagger UI disponible.
- Tests de integración con H2 + MockMvc.

Archivos clave
- src/main/java/org/zgo/auth/... (código)
- src/main/resources/jwtKeys/ (fallback PEM keys, usadas sólo si no pasas env vars)
- application.properties: configuración H2 y JWT expiration.
- README.md: (este archivo)

Configurar claves RSA (producción recomendado)
- Puedes inyectar las claves RSA en variables de entorno:
  - APP_JWT_PRIVATE_KEY: contenido completo del PEM privado (incluye -----BEGIN PRIVATE KEY----- ... -----END PRIVATE KEY-----)
  - APP_JWT_PUBLIC_KEY: contenido completo del PEM público (incluye -----BEGIN PUBLIC KEY----- ... -----END PUBLIC KEY-----)

Ejemplo (Linux / macOS):
```bash
export APP_JWT_PRIVATE_KEY="$(cat ~/keys/private.key.pem)"
export APP_JWT_PUBLIC_KEY="$(cat ~/keys/public.key.pem)"
```

Si no defines las variables, el servicio usa claves de ejemplo colocadas en src/main/resources/jwtKeys (útil para desarrollo/tests locales). NO subir claves reales al repo para entornos productivos.

Cómo ejecutar localmente
1. Descargar / clonar repo:
   git clone https://github.com/OmarZG/jwt-authentication-hexagonal.git
   cd jwt-authentication-hexagonal

2. Crear la rama con los cambios (opcional):
   git checkout -b feature/orgzgo-repackage-and-updates

3. (Opcional) Exportar claves RSA por variables de entorno como se indicó arriba.

4. Construir y ejecutar:
   mvn -U clean package
   mvn spring-boot:run

   La aplicación arrancará en http://localhost:8080

OpenAPI / Swagger
- UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

H2 Console
- URL: http://localhost:8080/h2-console
- JDBC URL por defecto: jdbc:h2:mem:securitydb
- Usuario: sa (contraseña vacía)

EndPoints principales
- POST /api/auth/register  { username, email, password, roles: ["USER"] or ["ADMIN"] }
- POST /api/auth/login     { username, password }
- POST /api/auth/refresh   { refreshToken }
- POST /api/auth/revoke    { refreshToken }
- GET  /api/auth/me        (requires Bearer access token)
- GET  /api/auth/admin     (requires ROLE_ADMIN)

Ejemplo flujo (curl)
1) Register:
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"secret","roles":["USER"]}'
```
Respuesta: { "accessToken": "...", "refreshToken": "..." }

2) Use access token:
```bash
curl -s http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <accessToken>"
```

3) Refresh (rotates refresh token):
```bash
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refreshToken>"}'
```
Respuesta: { "accessToken": "...", "refreshToken": "NEW_TOKEN" }

4) Revoke token explicitly:
```bash
curl -s -X POST http://localhost:8080/api/auth/revoke \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<token-to-revoke>"}'
```
Respuesta: 204 No Content

Tests
- Ejecutar:
  mvn test

Notas de seguridad
- No subir claves privadas al repo en producción.
- Rotación de refresh tokens ayuda a mitigar uso indebido, pero considera usar short-lived refresh tokens y detectores de reuse para mayor seguridad.
- En producción guarda claves en un Secret Manager o Vault y pásalas como variables de entorno.

Contacto
- Repo: https://github.com/OmarZG/jwt-authentication-hexagonal
- Autor: OmarZG
```