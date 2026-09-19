# Quarkus vs Spring Boot — AI Microservices Demo

## Arquitectura

```
                    ┌─────────────────────────────────────────┐
                    │            Config Server :8888           │
                    │     (Spring Cloud — filesystem mode)     │
                    └──────────┬──────────────────┬───────────┘
                               │                  │
              ┌────────────────▼──┐          ┌────▼────────────────┐
              │  order-quarkus    │          │   order-spring       │
              │  :8080  Reactive  │          │   :8085  Blocking    │
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

# Mac
brew install --cask docker

# Linux
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
```

### Java 21
```bash
# Windows
winget install EclipseAdoptium.Temurin.21.JDK

# Mac
brew install --cask temurin@21

# Linux / cualquier SO con SDKMAN
curl -s "https://get.sdkman.io" | bash
sdk install java 21-tem
```

### Maven 3.9+
```bash
# Windows
winget install Apache.Maven

# Mac
brew install maven

# Linux / SDKMAN
sdk install maven
```

### k6 (opcional — load testing)
```bash
# Windows
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

### 1. Configura el archivo de entorno
```bash
# Linux / Mac
cp .env.example .env

# Windows (PowerShell)
copy .env.example .env
```
Abre `.env` y reemplaza `your-gemini-api-key-here` con tu API key.  
Consíguela gratis en https://aistudio.google.com/app/apikey

### 2. Compila todos los módulos
> La primera vez descarga dependencias Maven — puede tardar 5-10 minutos.
```bash
mvn clean package -DskipTests
```

### 3. Levanta el stack
```bash
docker compose up --build -d
```

### 4. Verifica que todo está corriendo
```bash
docker compose ps
```
Todos los servicios deben aparecer como `healthy` o `running`.  
Si alguno falla: `docker compose logs <nombre-servicio>`

---

## URLs

| Servicio | URL |
|---|---|
| **Order API — Quarkus** (Swagger) | http://localhost:8080/q/swagger-ui |
| **Order API — Spring** (Swagger) | http://localhost:8085/swagger-ui |
| **AI Service** (Swagger) | http://localhost:8081/q/swagger-ui |
| **Notification SSE stream** | http://localhost:8082/api/notifications/stream |
| **Config Server** | http://localhost:8888/order-quarkus/default |
| **Kafka UI** | http://localhost:8086 |
| **Grafana** | http://localhost:3000 (admin / demo123) |
| **Prometheus** | http://localhost:9090/targets |

---

## Load test comparativo

```bash
k6 run benchmark/k6/load-test-comparison.js
```

Con k6 corriendo, abre Grafana → dashboard **"Quarkus vs Spring Boot — Demo Comparativo"** para ver métricas en tiempo real.

---

## Apagar el stack

```bash
# Solo detener contenedores
docker compose down

# Detener y borrar volúmenes (PostgreSQL + Grafana)
docker compose down -v
```

---

## Troubleshooting

| Problema | Solución |
|---|---|
| Stack no arranca | `docker compose down -v && mvn package -DskipTests && docker compose up --build -d` |
| Config Server 500 | Verificar que `mvn package` compiló el JAR de config-server |
| Gemini no responde | Verificar `GEMINI_API_KEY` en `.env` — debe ser válida y activa |
| Grafana sin datos | Esperar 30s; verificar targets en http://localhost:9090/targets |
| Kafka UI vacío | Crear al menos una orden — los topics se crean con el primer mensaje |
| Port 5432 en uso | Postgres expuesto en 5434/5435 — no interfiere con una instalación local |
