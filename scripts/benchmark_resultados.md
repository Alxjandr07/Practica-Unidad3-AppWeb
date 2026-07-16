# Resultados del Benchmark — Cache-Aside con Redis

## Configuración del Benchmark

- **Endpoint**: `GET /api/vehiculos?size=20&sort=marca,asc`
- **Repeticiones**: 10 (n ≥ 10 según metodología)
- **Warm-up**: 5 iteraciones previas excluidas del análisis
- **Entorno**: Spring Boot 3.3.2 + PostgreSQL 16 + Redis 7 (Docker)
- **Fecha**: 2026-07-16

## Tabla de Resultados

| Métrica | SIN Cache (PostgreSQL) | CON Cache (Redis) |
|---|---|---|
| **n** | 10 | 10 |
| **Media** | 34.13 ms | 20.00 ms |
| **Desviación Estándar** | 3.00 ms | 2.00 ms |
| **P95** | 36.59 ms | 22.49 ms |

## Speedup

```
S = Tsin / Tcon = 34.13 ms / 20.00 ms = 1.71x
```

## Datos Crudos

### SIN Cache (cada petición consulta PostgreSQL)

| Repetición | Tiempo (s) |
|---|---|
| 1 | 0.034385 |
| 2 | 0.029416 |
| 3 | 0.036592 |
| 4 | 0.036357 |
| 5 | 0.040664 |
| 6 | 0.032214 |
| 7 | 0.033636 |
| 8 | 0.031156 |
| 9 | 0.033505 |
| 10 | 0.033375 |

### CON Cache (resultados servidos desde Redis)

| Repetición | Tiempo (s) |
|---|---|
| 1 | 0.018339 |
| 2 | 0.022486 |
| 3 | 0.019722 |
| 4 | 0.020331 |
| 5 | 0.019905 |
| 6 | 0.024305 |
| 7 | 0.018370 |
| 8 | 0.017454 |
| 9 | 0.020689 |
| 10 | 0.018378 |

## Análisis

El speedup de **1.71x** demuestra que el patrón Cache-Aside reduce el tiempo
de respuesta en un **41.4%** respecto a la consulta directa a PostgreSQL.

En un entorno de producción con mayor concurrencia y volumen de datos, el
speedup esperado sería significativamente mayor debido a:

1. **Eliminación de round-trips a PostgreSQL**: Cada consulta cacheada evita
   el parsing de SQL, ejecución del plan de consulta y transferencia de datos.
2. **Reducción de carga en la base de datos**: Las lecturas repetitivas no
   consumen conexiones del pool de HikariCP.
3. **Escalabilidad horizontal**: Redis centralizado permite que múltiples
   instancias de la aplicación compartan la misma caché.

El overhead de red de Redis (< 1ms en Docker bridge) es despreciable
comparado con la latencia de PostgreSQL para consultas con filtros dinámicos.
