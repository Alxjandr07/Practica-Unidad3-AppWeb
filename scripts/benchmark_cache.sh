#!/usr/bin/env bash
# Benchmark del cache-aside (Anexo C, pregunta 1): ejecuta la consulta principal
# de listado N veces y mide el tiempo de respuesta, para comparar CON y SIN Redis.
#
# Uso:
#   1) SIN cache:
#        - en application.yml poner: spring.cache.type: none
#        - levantar el backend, correr:  ./scripts/benchmark_cache.sh sin_cache.csv
#   2) CON cache (cache-aside activo):
#        - revertir spring.cache.type: redis
#        - levantar el backend, correr:  ./scripts/benchmark_cache.sh con_cache.csv
#   3) Analizar ambos archivos:
#        python3 scripts/analizar_benchmark.py sin_cache.csv con_cache.csv
#
# Requiere: backend corriendo en http://localhost:8080 y sesion ya autenticada
# (cookie ACCESS_TOKEN guardada en cookies.txt via /api/auth/login).

set -euo pipefail

REPETICIONES="${2:-10}"
SALIDA="${1:-benchmark.csv}"
URL="http://localhost:8080/api/vehiculos?size=20&sort=marca,asc"
COOKIE_JAR="cookies.txt"

echo "repeticion,tiempo_segundos" > "$SALIDA"

for i in $(seq 1 "$REPETICIONES"); do
    tiempo=$(curl -s -o /dev/null -w "%{time_total}" -b "$COOKIE_JAR" "$URL")
    echo "$i,$tiempo" >> "$SALIDA"
    echo "Repeticion $i: ${tiempo}s"
done

echo "Resultados guardados en $SALIDA"
