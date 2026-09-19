# =================================================================
# Tiltfile — Quarkus vs Spring Boot Demo
#
# Uso:
#   tilt up                     → levanta TODO
#   tilt up kafka postgres-*    → solo infraestructura
#   tilt down                   → para y limpia
#
# Variables requeridas en .env (o GEMINI_API_KEY en tu shell):
#   GEMINI_API_KEY=tu-api-key
# =================================================================

# docker-compose lee .env automáticamente (GEMINI_API_KEY debe estar ahí o en el shell)

# ---- Gestión completa vía docker-compose ----
docker_compose('./docker-compose.yml')

# ---- Infraestructura base ----
dc_resource('zookeeper',        labels=['infra'],      resource_deps=[])
dc_resource('kafka',            labels=['infra'],      resource_deps=['zookeeper'])
dc_resource('kafka-ui',         labels=['infra'],      resource_deps=['kafka'])
dc_resource('postgres-quarkus', labels=['infra'])
dc_resource('postgres-spring',  labels=['infra'])
dc_resource('config-server',    labels=['infra'])

# ---- Servicios de aplicación ----
dc_resource('order-quarkus',        labels=['services'], resource_deps=['config-server', 'postgres-quarkus', 'kafka'])
dc_resource('order-spring',         labels=['services'], resource_deps=['config-server', 'postgres-spring', 'kafka'])
dc_resource('ai-quarkus',           labels=['services'], resource_deps=['config-server', 'kafka'])
dc_resource('notification-quarkus', labels=['services'], resource_deps=['config-server', 'kafka'])

# ---- Monitoring ----
dc_resource('prometheus', labels=['monitoring'])
dc_resource('grafana',    labels=['monitoring'], resource_deps=['prometheus'])

# ---- Links útiles en la UI de Tilt ----
dc_resource('order-quarkus',
    links=[
        link('http://localhost:8080/swagger-ui', 'Swagger UI — Quarkus'),
        link('http://localhost:8080/q/health',   'Health — Quarkus'),
    ]
)
dc_resource('order-spring',
    links=[
        link('http://localhost:8090/swagger-ui.html', 'Swagger UI — Spring'),
        link('http://localhost:8090/actuator/health', 'Health — Spring'),
    ]
)
dc_resource('kafka-ui',
    links=[link('http://localhost:8085', 'Kafka UI')]
)
dc_resource('grafana',
    links=[link('http://localhost:3000', 'Grafana (admin / demo123)')]
)
dc_resource('config-server',
    links=[
        link('http://localhost:8888/order-quarkus/default', 'Config — order-quarkus'),
        link('http://localhost:8888/order-spring/default',   'Config — order-spring'),
        link('http://localhost:8888/ai-quarkus/default',     'Config — ai-quarkus'),
    ]
)
