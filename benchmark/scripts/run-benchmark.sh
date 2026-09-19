#!/usr/bin/env bash
# ============================================================
# Script de benchmark completo — muestra startup + load test
# Uso: bash benchmark/scripts/run-benchmark.sh
# ============================================================

QUARKUS_URL=${QUARKUS_URL:-"http://localhost:8080"}
SPRING_URL=${SPRING_URL:-"http://localhost:8090"}

echo "=============================================="
echo "  QUARKUS vs SPRING BOOT — BENCHMARK SUITE"
echo "=============================================="

echo ""
echo "📊 Verificando servicios..."
curl -sf "$QUARKUS_URL/q/health" > /dev/null && echo "✅ Quarkus: UP" || echo "❌ Quarkus: DOWN"
curl -sf "$SPRING_URL/actuator/health" > /dev/null && echo "✅ Spring:  UP" || echo "❌ Spring:  DOWN"

echo ""
echo "🔢 Memoria actual (via Docker):"
echo "  Quarkus:  $(docker stats order-quarkus --no-stream --format '{{.MemUsage}}' 2>/dev/null || echo 'N/A')"
echo "  Spring:   $(docker stats order-spring  --no-stream --format '{{.MemUsage}}' 2>/dev/null || echo 'N/A')"

echo ""
echo "🚀 Iniciando load test comparativo (2 minutos)..."
echo "   Abre Grafana en http://localhost:3000 para ver en tiempo real"
echo ""

k6 run \
  -e QUARKUS_URL="$QUARKUS_URL" \
  -e SPRING_URL="$SPRING_URL" \
  --out influxdb=http://localhost:8086/k6 \
  benchmark/k6/load-test-comparison.js

echo ""
echo "✅ Benchmark completado. Revisa Grafana para el dashboard completo."
