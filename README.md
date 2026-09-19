# Quarkus vs Spring Boot — Demo para la charla

## Stack completo

```
                    ┌─────────────────────────────────────────┐
                    │            Config Server :8888           │
                    │     (Spring Cloud — filesystem mode)     │
                    └──────────┬──────────────────┬───────────┘
                               │                  │
              ┌────────────────▼──┐          ┌────▼────────────────┐
              │  order-quarkus    │          │   order-spring       │
              │  :8080  Reactive  │          │   :8090  Blocking    │
              │  Hibernate React. │          │   JPA + WebMVC       │
              └────────┬──────────┘          └────────┬────────────┘
                       │ Kafka orders.created          │
              ┌────────▼──────────────────────────────▼────────────┐
              │                   Kafka :9092                       │
              └────────────────────────┬────────────────────────────┘
                                       │ orders.created
                              ┌────────▼────────┐
                              │   ai-quarkus     │
                              │   :8081          │
                              │   LangChain4j    │
                              │   Gemini 2.5 Flash│
                              └────────┬─────────┘
                                       │ ai.analysis
                              ┌────────▼─────────┐
                              │notification-quark │
                              │   :8082  SSE      │
                              └──────────────────┘

  Monitoring: Prometheus :9090 → Grafana :3000
  Kafka UI: :8085
```

---

## Requisitos previos

### Docker Desktop
```bash
# Windows
winget install Docker.DockerDesktop
# o descarga desde https://www.docker.com/products/docker-desktop/

# Mac
brew install --cask docker

# Linux (Docker Engine)
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
```

### Java 21
```bash
# Windows (con scoop)
scoop install temurin21-jdk
# o con winget
winget install EclipseAdoptium.Temurin.21.JDK

# Mac
brew install --cask temurin@21

# Linux (Ubuntu/Debian)
sudo apt install -y temurin-21-jdk
# o con SDKMAN (cualquier SO)
curl -s "https://get.sdkman.io" | bash
sdk install java 21-tem
```

### Maven 3.9+
```bash
# Windows
scoop install maven
# o con winget
winget install Apache.Maven

# Mac
brew install maven

# Linux
sudo apt install -y maven
# o con SDKMAN
sdk install maven
```

### Tilt (opcional — UI de desarrollo)
```bash
# Windows
scoop bucket add tilt-dev https://github.com/tilt-dev/scoop-bucket
scoop install tilt

# Mac
brew install tilt-dev/tap/tilt

# Linux
curl -fsSL https://raw.githubusercontent.com/tilt-dev/tilt/master/scripts/install.sh | bash
```

### k6 (opcional — load testing)
```bash
# Windows
scoop install k6
# o con winget
winget install k6

# Mac
brew install k6

# Linux (Ubuntu/Debian)
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg \
  --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" \
  | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt update && sudo apt install k6
```

---

## Inicio rápido

### 1. Configura tu API key de Gemini
```
# Edita .env y pon tu key real:
GEMINI_API_KEY=tu-key-aqui
```

### 2. Compila todos los módulos
```bash
mvn clean package -DskipTests
```

### 3. Levanta el stack
```bash
# Opción A: docker compose directo
docker compose up -d

# Opción B: Tilt (UI visual con logs por servicio)
tilt up
```

### 4. Verifica que todo está corriendo
```bash
docker compose ps
```

---

## URLs del demo

| Servicio | URL | Para qué |
|---|---|---|
| **Order API — Quarkus** | http://localhost:8080/swagger-ui | Crear órdenes (reactivo) |
| **Order API — Spring** | http://localhost:8090/swagger-ui.html | Crear órdenes (bloqueante) |
| **Config Server** | http://localhost:8888/order-quarkus/default | Ver config centralizada |
| **Kafka UI** | http://localhost:8085 | Ver topics y mensajes en tiempo real |
| **Grafana** | http://localhost:3000 | Dashboard comparativo (admin / demo123) |
| **Prometheus** | http://localhost:9090/targets | Estado del scraping |
| **SSE Notifications** | http://localhost:8082/api/notifications/stream | Stream de análisis AI |

---

## Flujo de la charla (paso a paso)

### Paso 1 — Arranque y arquitectura

Mostrar el diagrama de arriba. Puntos clave:
- `shared-domain`: dominio puro Java, **cero imports** de Quarkus, Spring, JPA o Kafka
- Los dos servicios de order implementan los **mismos puertos** (interfaces del dominio)
- La diferencia es solo en los adaptadores de infraestructura

### Paso 2 — Config Server centralizado

Abrir en el browser: **http://localhost:8888/order-quarkus/default**

El JSON muestra toda la configuración de Kafka, Hibernate y Kubernetes centralizada. Ningún secreto. Cambiar el modelo de Gemini en `ai-quarkus.yml` no requiere redesplegar el servicio.

### Paso 3 — Crear una orden y ver el flujo completo

En Swagger de Quarkus (http://localhost:8080/swagger-ui), ejecutar:

```json
POST /api/orders
{
  "customerId": "cliente-vip-001",
  "customerEmail": "vip@demo.com",
  "country": "PE",
  "items": [
    {
      "productId": "laptop-pro",
      "productName": "Laptop Pro 16",
      "quantity": 3,
      "price": 2500.00
    }
  ]
}
```

Luego, en **Kafka UI** (http://localhost:8085):
1. Topic `orders.created` → ver el mensaje con la orden
2. Esperar ~3-5 segundos → Topic `ai.analysis` → ver el análisis de fraude devuelto por Gemini

En paralelo, abrir el **SSE stream** en otra pestaña:
```
http://localhost:8082/api/notifications/stream
```
Se ve el evento de análisis AI llegar en tiempo real.

### Paso 4 — El mismo flujo con Spring Boot

Repetir el paso 3 con Swagger de Spring (http://localhost:8090/swagger-ui.html).
El código del dominio es idéntico — solo cambia el framework del adaptador.

### Paso 5 — Load test en vivo con k6

```bash
# Instalar k6 (si no está)
scoop install k6   # Windows

# Ejecutar benchmark comparativo
k6 run benchmark/k6/load-test-comparison.js
```

Mientras corre, abrir **Grafana** (http://localhost:3000 → admin / demo123):
- Dashboard: **"Quarkus vs Spring Boot — Demo Comparativo"**

Lo que verás en los paneles:
- **Throughput**: requests/seg lado a lado
- **Latencia P99**: Quarkus generalmente menor en alta carga
- **Memoria JVM**: Quarkus ~80MB heap vs Spring ~250MB ← el "wow" visual
- **Threads**: Quarkus event loop vs Spring thread-per-request

### Paso 6 — El "wow" del startup

```bash
# Detener los servicios de orden
docker compose stop order-quarkus order-spring

# Arrancarlos y contar en voz alta
docker compose start order-quarkus   # ~2-3 segundos
docker compose start order-spring    # ~8-12 segundos
```

O en Kubernetes (con k3d):
```bash
k3d cluster create demo
kubectl apply -f k8s/quarkus/
kubectl apply -f k8s/spring/
kubectl get pods -w   # Observar cuál llega a Ready primero
```

---

## Ver Grafana paso a paso

1. Abrir http://localhost:3000
2. Login: `admin` / `demo123`
3. Menú izquierdo → **Dashboards** → carpeta **"Demo — Quarkus vs Spring"**
4. Abrir **"Quarkus vs Spring Boot — Demo Comparativo"**

El dashboard se actualiza cada 5 segundos. Con k6 corriendo se ven las métricas en tiempo real.

---

## El "wow" con Gemini AI — orden de alto riesgo

Crear una orden que Gemini marque como HIGH_RISK:
```json
POST /api/orders
{
  "customerId": "new-user-001",
  "customerEmail": "x@temp.com",
  "country": "PE",
  "items": [
    {
      "productId": "iphone-15-pro",
      "productName": "iPhone 15 Pro Max",
      "quantity": 10,
      "price": 1500.00
    }
  ]
}
```

En Kafka UI → topic `ai.analysis`: ver la respuesta de Gemini con `riskLevel: HIGH`.

---

## Troubleshooting

| Problema | Solución |
|---|---|
| Stack no arranca | `docker compose down -v && mvn package -DskipTests && docker compose up -d` |
| Config Server 500 | Verificar que `mvn package` compiló el JAR de config-server |
| Gemini no responde | Verificar `GEMINI_API_KEY` en `.env` — debe ser una key válida y activa |
| Grafana sin datos | Esperar 30s; verificar targets en http://localhost:9090/targets |
| Kafka UI vacío | Crear al menos una orden — los topics se crean con el primer mensaje |
| Port 5432 en uso | Ya configurado en 5434/5435 — tu Postgres local no interfiere |
