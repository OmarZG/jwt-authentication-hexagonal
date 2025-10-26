# jwt-authentication-hexagonal

Proyecto de ejemplo: autenticación con **JWT (RSA)** + **refresh tokens persistentes** implementado bajo una **Arquitectura Hexagonal (Clean Architecture)** con **MapStruct** para los mapeos entre capas.

**Paquete base:** `org.zgo.auth`

---

## 🧱 Arquitectura y principios

El proyecto sigue los principios de **Arquitectura Hexagonal (Ports & Adapters)**:

* **Capa de Aplicación (Core):** contiene la lógica de negocio pura (use cases) e interfaces (`ports`) que definen los contratos de entrada/salida.
* **Capa de Dominio:** contiene las entidades, modelos y excepciones propias del negocio.
* **Capa de Infraestructura:** implementa los adaptadores (repositorios, servicios JWT, configuración de seguridad, etc.).
* **Capa Web:** maneja la comunicación HTTP (controladores, DTOs y mapeos con MapStruct).

El **mapeo entre capas** se gestiona automáticamente con **MapStruct**, lo que evita duplicación y errores en conversiones:

* Web DTOs ↔ Application Models ↔ Domain Entities.

---

## ⚙️ Tecnologías principales

* **Java 21**
* **Spring Boot 3.x**
* **Spring Framework 6.x**
* **MapStruct** para mapeos automáticos
* **Lombok** (con `lombok-mapstruct-binding`)
* **JWT firmado con RS256 (RSA private/public key)**
* **H2 Database** (memoria por defecto)
* **Spring Security 6**
* **Swagger / OpenAPI**
* **JUnit + MockMvc** (tests de integración)

---

## 🔐 Generación de claves RSA con OpenSSL

Ejecutar los siguientes comandos desde la terminal:

```bash
# Generar clave privada RSA 2048 bits
openssl genpkey -algorithm RSA -out private.key.pem -pkeyopt rsa_keygen_bits:2048

# Derivar la clave pública
openssl rsa -pubout -in private.key.pem -out public.key.pem
```

---

## 🧩 Estructura del proyecto

```
│   pom.xml
│   postman_collection_jwt_auth.json
│   README.md      
├───src
│   ├───main
│   │   ├───java/org/zgo/auth
│   │   │   │   JwtAuthenticationHexagonalApplication.java
│   │   │   ├───application
│   │   │   │   ├───port
│   │   │   │   │   ├───in
│   │   │   │   │   │       AuthResult.java
│   │   │   │   │   │       AuthUseCase.java
│   │   │   │   │   │       LoginParameters.java
│   │   │   │   │   │       RefreshTokenUseCase.java
│   │   │   │   │   │       RegisterUserData.java
│   │   │   │   │   │       UserResult.java
│   │   │   │   │   └───out
│   │   │   │   │           RefreshTokenPersistencePort.java
│   │   │   │   │           UserPersistencePort.java
│   │   │   │   └───service
│   │   │   │           AuthServiceImpl.java
│   │   │   │           RefreshTokenServiceImpl.java
│   │   │   ├───domain
│   │   │   │   ├───exception/custom
│   │   │   │   │       InvalidCredentialsException.java
│   │   │   │   │       UserAlreadyExistsException.java
│   │   │   │   └───model
│   │   │   │           RefreshToken.java
│   │   │   │           Role.java
│   │   │   │           User.java
│   │   │   └───infrastructure
│   │   │       ├───config
│   │   │       │   │   JwtAuthenticationEntryPoint.java
│   │   │       │   │   OpenApiConfig.java
│   │   │       │   │   SecurityConfig.java
│   │   │       │   ├───filter
│   │   │       │   │       JwtAuthenticationFilter.java
│   │   │       │   └───properties
│   │   │       │           SecurityProperties.java
│   │   │       ├───persistence
│   │   │       │   ├───adapter
│   │   │       │   │       RefreshTokenPersistenceAdapter.java
│   │   │       │   │       UserPersistenceAdapter.java
│   │   │       │   ├───entity
│   │   │       │   │       RefreshTokenEntity.java
│   │   │       │   │       UserEntity.java
│   │   │       │   ├───mapper
│   │   │       │   │       RefreshTokenMapper.java
│   │   │       │   │       UserMapper.java
│   │   │       │   └───repository
│   │   │       │           RefreshTokenRepository.java
│   │   │       │           UserRepository.java
│   │   │       ├───service
│   │   │       │       JwtService.java
│   │   │       │       UserDetailsServiceImpl.java
│   │   │       └───web
│   │   │           ├───dto
│   │   │           │   ├───request
│   │   │           │   │       LoginRequest.java
│   │   │           │   │       RefreshTokenRequest.java
│   │   │           │   │       RegisterRequest.java
│   │   │           │   │       RevokeRefreshRequest.java
│   │   │           │   └───response
│   │   │           │           AuthenticationResponse.java
│   │   │           │           ErrorResponse.java
│   │   │           │           UserResponse.java
│   │   │           ├───exception
│   │   │           │       GlobalExceptionHandler.java
│   │   │           ├───in
│   │   │           │       AuthenticationController.java
│   │   │           └───mapper
│   │   │                   AuthMapper.java
│   │   └───resources
│   │       │   application.yml
│   │       └───jwtKeys
│   │               private.key.pem
│   │               public.key.pem
│   └───test/java/org/zgo/auth
│       ├───config
│       │       TestConfig.java
│       ├───controller
│       │       AuthenticationControllerTest.java
│       └───service
│               AuthServiceIntegrationTest.java
```

---

## 🔧 Configuración de claves RSA

Puedes inyectar las claves RSA en variables de entorno:

```bash
export APP_JWT_PRIVATE_KEY="$(cat ~/keys/private.key.pem)"
export APP_JWT_PUBLIC_KEY="$(cat ~/keys/public.key.pem)"
```

Si no defines las variables, se usarán las claves por defecto ubicadas en
`src/main/resources/jwtKeys/` (solo para desarrollo).

⚠️ **No incluir claves reales en repositorios públicos.**

---

## 🚀 Ejecución local

```bash
mvn -U clean package
mvn spring-boot:run
```

La aplicación se ejecutará en:
👉 [http://localhost:8080](http://localhost:8080)

---

## 📘 OpenAPI / Swagger

* UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🗄️ H2 Console

* URL: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
* JDBC URL: `jdbc:h2:mem:securitydb`
* Usuario: `sa`

---

## 🔑 Endpoints principales

| Método | Endpoint             | Descripción               |
| ------ | -------------------- | ------------------------- |
| POST   | `/api/auth/register` | Registro de nuevo usuario |
| POST   | `/api/auth/login`    | Inicio de sesión          |
| POST   | `/api/auth/refresh`  | Renovación de token       |
| POST   | `/api/auth/revoke`   | Revocar refresh token     |
| GET    | `/api/auth/me`       | Usuario autenticado       |
| GET    | `/api/auth/admin`    | Requiere ROLE_ADMIN       |

---

## 🧪 Tests

```bash
mvn test
```

Incluye pruebas unitarias y de integración con H2 + MockMvc.

---

## 🔒 Notas de seguridad

* No subir claves privadas a repositorios.
* En producción, usar un **Secret Manager o Vault**.
* Aplicar **rotación y caducidad corta de refresh tokens**.
* **MapStruct** mejora la mantenibilidad y evita errores en conversiones de datos.

---
