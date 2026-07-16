#!/usr/bin/env python3
"""
Analiza los CSV generados por benchmark_cache.sh y calcula:
  - Media y desviacion estandar (n >= 10 repeticiones, segun la metodologia pedida)
  - Percentil 95 (P95)
  - Speedup: S = Tsin / Tcon

Uso:
    python3 analizar_benchmark.py sin_cache.csv con_cache.csv
"""
import sys
import csv
import statistics


def cargar_tiempos(ruta):
    with open(ruta) as f:
        return [float(row["tiempo_segundos"]) for row in csv.DictReader(f)]


def percentil_95(datos):
    datos_ordenados = sorted(datos)
    indice = int(round(0.95 * (len(datos_ordenados) - 1)))
    return datos_ordenados[indice]


def resumir(nombre, tiempos):
    media = statistics.mean(tiempos)
    desviacion = statistics.stdev(tiempos) if len(tiempos) > 1 else 0.0
    p95 = percentil_95(tiempos)
    print(f"\n{nombre} (n={len(tiempos)})")
    print(f"  Media:              {media:.4f} s")
    print(f"  Desviacion estandar: {desviacion:.4f} s")
    print(f"  P95:                {p95:.4f} s")
    return media, desviacion, p95


def main():
    if len(sys.argv) != 3:
        print("Uso: python3 analizar_benchmark.py sin_cache.csv con_cache.csv")
        sys.exit(1)

    tiempos_sin = cargar_tiempos(sys.argv[1])
    tiempos_con = cargar_tiempos(sys.argv[2])

    media_sin, _, _ = resumir("SIN cache (Postgres directo)", tiempos_sin)
    media_con, _, _ = resumir("CON cache (Redis, cache-aside)", tiempos_con)

    speedup = media_sin / media_con if media_con > 0 else float("inf")
    print(f"\nSpeedup  S = Tsin / Tcon = {media_sin:.4f} / {media_con:.4f} = {speedup:.2f}x")


if __name__ == "__main__":
    main()
