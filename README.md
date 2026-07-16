# Practica Experimental Unidad III — Aplicaciones Web [111]

**Universidad Tecnica Estatal de Quevedo (UTEQ)** · 5to Nivel — Software
Docente: Ing. Guerrero Ulloa Gleiston Ciceron

Backend de la Practica Experimental de la Unidad III (Temas 9-12), stack
**Java 21 / Spring Boot 3.3 / PostgreSQL 16 / Redis 7**. Cubre:

- (a) Gestion de estado stateless con JWT en cookie HttpOnly + blacklist de JTI en Redis (logout real).
- (b) CRUD completo con Spring Data JPA, paginacion (`Pageable`) y >= 50 registros semilla (Flyway `V3__datos_semilla.sql`).
- (c) Arquitectura en capas (Controller → Service → Repository → Entity) + patron Cache-Aside con Redis (`@Cacheable` / `@CacheEvict`).

> Pendiente para la siguiente entrega (no incluido en este paquete de codigo):
> diagrama de clases UML, ERD desde pgAdmin, C4 (niveles 1-2), ADRs redactados
> y el informe tecnico con las respuestas del Anexo C. Se generan aparte.

---

## 1. Requisitos

- JDK 21
- Maven 3.8+
- Docker y Docker Compose (para Postgres + Redis locales)

## 2. Levantar la infraestructura

```bash
docker compose up -d
```

Esto levanta:
- PostgreSQL 16 en `localhost:5432` (db `unidad3_db`, user/pass `postgres`/`postgres`)
- Redis 7 en `localhost:6379`
- pgAdmin en `http://localhost:5050` (opcional, para el ERD del punto 3 de la guia)

## 3. Ejecutar el backend

```bash
mvn spring-boot:run
```

Al arrancar, Flyway aplica automaticamente `V1`, `V2` y `V3` (esquema, usuario
admin y 60 vehiculos semilla). El backend queda en `http://localhost:8080`.

## 4. Probar la API

### 4.1 Login (guarda la cookie JWT en `cookies.txt`)

```bash
curl -i -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin#2026"}'
```

### 4.2 Listar vehiculos (paginado + filtros + orden dinamico)

```bash
curl -b cookies.txt "http://localhost:8080/api/vehiculos?page=0&size=10&estado=ACTIVO&sort=marca,asc"
```

### 4.3 Crear / actualizar / eliminar

```bash
curl -b cookies.txt -X POST http://localhost:8080/api/vehiculos \
  -H "Content-Type: application/json" \
  -d '{"placa":"PQ0000","marca":"Hino","modelo":"AK8","anioFabricacion":2022,"capacidadPasajeros":30,"tipoServicio":"URBANO"}'
```

### 4.4 Logout (invalida el JWT vigente vía blacklist en Redis)

```bash
curl -i -b cookies.txt -c cookies.txt -X POST http://localhost:8080/api/auth/logout
# Cualquier request posterior con el mismo token (mismo cookies.txt) ya devuelve 401/403
```

Para comprobarlo en Redis directamente:

```bash
docker exec -it unidad3-redis redis-cli KEYS "jwt:blacklist:*"
```

## 5. Pruebas unitarias

```bash
mvn test
```

- `VehiculoRepositoryTest`: capa Repository con **H2 en memoria** (perfil `test`), paginacion, filtros y Specifications.
- `VehiculoServiceImplTest`: reglas de negocio con Mockito (placa duplicada, recurso no encontrado, mapeo de campos).
- `JwtServiceTest` / `TokenBlacklistServiceTest`: generacion/validacion de JWT y logica de blacklist en Redis.

> Nota de cobertura (>= 70% pedido en la guia): agrega el plugin `jacoco-maven-plugin`
> a tu `pom.xml` local (`mvn org.jacoco:jacoco-maven-plugin:prepare-agent test jacoco:report`)
> si tu instructor pide el reporte HTML; se omitio aqui para no acoplar el pom a un plugin
> que tu equipo puede preferir configurar con su propio umbral.

## 6. Benchmark de Cache-Aside (Anexo C, pregunta 1)

```bash
# 1) SIN cache: en application.yml, cambia spring.cache.type: none, reinicia el backend
./scripts/benchmark_cache.sh sin_cache.csv

# 2) CON cache: revierte spring.cache.type: redis, reinicia el backend
./scripts/benchmark_cache.sh con_cache.csv

# 3) Analiza ambos (media, desviacion estandar, P95, Speedup S = Tsin/Tcon)
python3 scripts/analizar_benchmark.py sin_cache.csv con_cache.csv
```

## 7. Estructura del proyecto

```
src/main/java/com/uteq/unidad3/
├── config/        SecurityConfig, RedisConfig (cache-aside, TTL por cache)
├── security/      JwtService, TokenBlacklistService, JwtAuthFilter, CookieUtil
├── domain/        Usuario, Vehiculo, Rol, TipoServicio, EstadoVehiculo
├── repository/    UsuarioRepository, VehiculoRepository, VehiculoSpecifications
├── service/       AuthService, VehiculoService/Impl (@Cacheable/@CacheEvict aqui)
├── controller/     AuthController, VehiculoController
├── dto/           LoginRequest/Response, VehiculoRequest/Response
└── exception/     GlobalExceptionHandler y excepciones de negocio
src/main/resources/db/migration/   V1 (esquema), V2 (admin), V3 (60 vehiculos semilla)
scripts/                            benchmark_cache.sh, analizar_benchmark.py
```

## 8. Usuario semilla

| username | password    | rol   |
|----------|-------------|-------|
| admin    | Admin#2026  | ADMIN |

---
*Este README documenta unicamente el paquete de codigo (puntos 2a-2c de la guia).
Los puntos 3-7 (UML/ERD/C4/ADRs/informe con Anexo C) se entregan en un documento
tecnico aparte.*
