# ADR-003: Estrategia de Caché y Blacklist JWT con Redis

## Estado

Aprobado (2026-07-16)

## Contexto

El PFC requiere resolver dos problemas no funcionales transversales:

1. **Rendimiento de consultas**: La operación principal del sistema (listado
   de vehículos con filtros, paginación y ordenamiento) se ejecuta contra
   PostgreSQL con consultas JPQL dinámicas generadas por JPA Specifications.
   Con 60 registros semilla y crecimiento esperado, cada consulta implica
   un round-trip a la base de datos que incluye: parsing del SQL por
   PostgreSQL, ejecución del plan de consulta, transferencia de resultados
   y deserialización en entidades JPA. Se busca reducir la latencia de
   las consultas repetitivas mediante caché en memoria.

2. **Invalidación de tokens JWT**: La autenticación stateless con JWT
   almacena el token en una cookie HttpOnly del navegador. El servidor
   no mantiene estado de sesión. Sin embargo, se requiere un mecanismo
   de logout efectivo que invalide el token antes de su expiración natural.
   Se necesita un almacenamiento distribuido para registrar los IDs (jti)
   de tokens revocados y verificarlos en cada petición autenticada.

El equipo debe seleccionar una tecnología de caché que satisfaga ambas
necesidades simultáneamente, evaluando: Redis, Ehcache, Caffeine,
Hazelcast y la caché de PostgreSQL.

## Decisión

Implementaremos **Redis 7** como store unificado para ambos casos de uso:

### 3.1 Caché de Datos — Patrón Cache-Aside

El patrón Cache-Aside (lazy loading) se implementa mediante las anotaciones
de Spring Cache sobre Redis:

- `@Cacheable("vehiculosListado")` en el método `buscar()` del Service:
  Antes de ejecutar la consulta JPA, Spring verifica si existe una clave
  en Redis con la serialización JSON de los resultados. Si existe, retorna
  el cache hit sin tocar la base de datos. Si no existe, ejecuta la
  consulta, almacena el resultado en Redis con TTL de 5 minutos, y lo retorna.

- `@Cacheable("vehiculosPorId")` en `obtenerPorId()`: TTL de 30 minutos
  para entidades individuales (menos frecuencia de cambio).

- `@CacheEvict` en `crear()`, `actualizar()`, `eliminar()`: Invalidación
  inmediata de ambas cachés al detectar una escritura, garantizando
  consistencia eventual.

La configuración de Redis como CacheManager utiliza serialización JSON con
`GenericJackson2JsonRedisSerializer` para permitir la inspección manual
de claves desde la CLI de Redis durante debugging.

### 3.2 Blacklist de JWT — Almacenamiento de Tokens Revocados

Cada token JWT contiene un claim `jti` (JWT ID) con un UUID único. Al
ejecutar logout:

1. Se extrae el `jti` y el tiempo restante de expiración del token.
2. Se almacena en Redis la clave `jwt:blacklist:{jti}` con valor `"revocado"`.
3. El TTL de la clave se establece igual al tiempo restante de expiración
   del token (se calcula con `JwtService.msRestantes()`).
4. Cuando el TTL expira, Redis elimina la clave automáticamente, limpiando
   la blacklist sin intervención manual.

En cada petición autenticada, el `JwtAuthFilter` verifica:
```
IF redis.hasKey("jwt:blacklist:{jti}") THEN reject (401)
ELSE permitir
```

Esta estrategia es descrita por Redhat (2023) como el estándar para
invalidación de JWT stateless sin mantener sesiones en el servidor.

### 3.3 Justificación de Redis sobre Alternativas

| Criterio | Redis 7 | Ehcache 3 | Caffeine | PostgreSQL |
|---|---|---|---|---|
| Modelo de datos | Key-Value enriquecido (Strings, Hashes, Sets, Sorted Sets) | Key-Value en memoria local | Key-Value en memoria local | Tablas SQL |
| Persistencia | RDB snapshots + AOF (configurable) | Opcional (disk) | No (solo memoria) | Completa |
| TTL nativo | ✅ Sí, por clave | ✅ Sí | ✅ Sí | No (requiere lógica custom) |
| Distribuido | ✅ Multi-nodo (clustering) | ❌ Solo local JVM | ❌ Solo local JVM | ✅ Pero con overhead de SQL |
| Compartido entre instancias | ✅ Sí (Redis separado) | ❌ No (cada JVM tiene su caché) | ❌ No | N/A |
| Uso para blacklist JWT | ✅ TTL automático de claves | ⚠️ Posible pero no idiomático | ⚠️ Sin persistencia | ❌ Overkill |
| Overhead de red | Mínimo (protocolo RESP) | N/A (local) | N/A (local) | Alto (TCP + SQL parsing) |
| Memoria | Eficiente (data structures optimizadas) | JVM heap | JVM heap | Disco |

**Caffeine y Ehcache** son caches locales a la JVM. Si la aplicación se
escala a múltiples instancias, cada una tendría su propio cache y la
blacklist de una instancia no sería visible para otra. Redis resuelve
este problema al ser un store centralizado.

**PostgreSQL** como caché requeriría queries SQL para verificar la blacklist,
lo cual añade latencia innecesaria y contamina la base de datos operacional
con datos transitorios.

## Consecuencias

**Positivas:**
- Un solo servicio de infraestructura (Redis) resuelve dos problemas
  distintos, reduciendo la complejidad operacional del docker-compose.
- El TTL automático de Redis gestiona la expiración de tanto la caché
  como la blacklist sin código adicional de limpieza.
- La serialización JSON permite inspección directa desde `redis-cli`
  durante desarrollo: `GET "vehiculosListado::..." ` retorna el JSON
  legible del listado cacheado.
- El speedup medible de la caché (S = Tsin / Tcon) proporciona evidencia
  cuantitativa del beneficio de Redis, directamente utilizable en el
  informe técnico.

**Negativas:**
- Punto único de fallo: si Redis cae, las consultas degradan al
  rendimiento de PostgreSQL (cache miss) y el logout no invalida tokens
  hasta que Redis se recupere. Se mitiga con el retry de Spring Cache
  y el TTL de los tokens como fallback natural.
- Overhead de red: cada consulta requiere un round-trip adicional a
  Redis antes de ir a PostgreSQL. Con una red local (Docker bridge),
  la latencia es < 1ms, lo cual es despreciable comparado con la
  latencia de PostgreSQL (5-50ms según complejidad de query).
- Consumo de memoria Redis: 60 registros serializados en JSON ocupan
  aproximadamente 50-100KB, lo cual es insignificante para la memoria
  asignada al contenedor.

## Alternativas Consideradas

| Alternativa | Ventaja | Razón de rechazo |
|---|---|---|
| **Caffeine (caché local)** | Latencia nanosegunda, sin overhead de red | No compartido entre instancias. La blacklist JWT no funciona en modo distribuido. Sin persistencia: reinicio del servidor pierde toda la caché y la blacklist. |
| **Ehcache 3 + disk persistence** | Caché local con opción de persistir a disco | Misma limitación de Caffeine para blacklist distribuida. Complejidad adicional de configurar disk tiers. |
| **PostgreSQL como blacklist** | No requiere servicio adicional | Cada verificación de token implica un query SQL. Alto overhead para una operación que ocurre en cada petición autenticada. Contaminación de la BD operacional con datos transitorios (tokens expirados). |
| **Sin caché (solo Redis blacklist)** | Simplifica la arquitectura | No demuestra el patrón Cache-Aside requerido por la guía. No justifica la presencia de Redis en el stack. No permite medir speedup para el Anexo C. |

## Referencias

- Redis Ltd. (2024). *Redis Documentation — TTL Commands*.
  https://redis.io/docs/latest/develop/learn/how-to-expire-keys/
- Red Hat. (2023). *API Gateway — JWT Token Blacklist Pattern*.
  https://www.redhat.com/en/topics/api/jwt-token-blacklist
- Burnette, E. (2009). *Head First Design Patterns*. O'Reilly Media.
  (Patrón Cache-Aside, pp. 394-401).
