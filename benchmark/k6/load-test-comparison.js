import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// ============================================================
// BENCHMARK: Quarkus vs Spring Boot — Comparación directa
// Uso: k6 run benchmark/k6/load-test-comparison.js
// ============================================================

const QUARKUS_URL = __ENV.QUARKUS_URL || 'http://localhost:8080';
const SPRING_URL  = __ENV.SPRING_URL  || 'http://localhost:8090';

// Métricas personalizadas para comparar lado a lado
const quarkusErrorRate  = new Rate('quarkus_errors');
const springErrorRate   = new Rate('spring_errors');
const quarkusDuration   = new Trend('quarkus_duration', true);
const springDuration    = new Trend('spring_duration', true);
const quarkusRequests   = new Counter('quarkus_requests_total');
const springRequests    = new Counter('spring_requests_total');

export const options = {
  stages: [
    { duration: '20s', target: 10  },  // Calentamiento
    { duration: '30s', target: 50  },  // Carga moderada
    { duration: '30s', target: 150 },  // Carga alta
    { duration: '30s', target: 300 },  // Carga extrema
    { duration: '20s', target: 0   },  // Enfriamiento
  ],
  thresholds: {
    'quarkus_duration': ['p(95)<200', 'p(99)<500'],
    'spring_duration':  ['p(95)<500'],
    'quarkus_errors':   ['rate<0.01'],
    'spring_errors':    ['rate<0.05'],
  },
};

const orderPayload = JSON.stringify({
  customerId: 'customer-demo-001',
  customerEmail: 'demo@example.com',
  country: 'PE',
  items: [
    { productId: 'prod-1', productName: 'Laptop', quantity: 1, price: 1200.00 },
    { productId: 'prod-2', productName: 'Mouse',  quantity: 2, price: 25.00  },
  ],
});

const headers = { 'Content-Type': 'application/json' };

export default function () {
  // Golpear ambos servicios en paralelo con http.batch
  const responses = http.batch([
    ['POST', `${QUARKUS_URL}/api/orders`, orderPayload, { headers, tags: { framework: 'quarkus' } }],
    ['POST', `${SPRING_URL}/api/orders`,  orderPayload, { headers, tags: { framework: 'spring'  } }],
  ]);

  const quarkusRes = responses[0];
  const springRes  = responses[1];

  // Registrar métricas de Quarkus
  quarkusDuration.add(quarkusRes.timings.duration);
  quarkusRequests.add(1);
  const quarkusOk = check(quarkusRes, {
    'quarkus status 201': (r) => r.status === 201,
    'quarkus < 200ms':    (r) => r.timings.duration < 200,
  });
  quarkusErrorRate.add(!quarkusOk);

  // Registrar métricas de Spring
  springDuration.add(springRes.timings.duration);
  springRequests.add(1);
  const springOk = check(springRes, {
    'spring status 201': (r) => r.status === 201,
    'spring < 500ms':    (r) => r.timings.duration < 500,
  });
  springErrorRate.add(!springOk);

  sleep(0.1);
}

export function handleSummary(data) {
  const qP50  = data.metrics.quarkus_duration?.values?.['p(50)']?.toFixed(2)  || 'N/A';
  const qP95  = data.metrics.quarkus_duration?.values?.['p(95)']?.toFixed(2)  || 'N/A';
  const qP99  = data.metrics.quarkus_duration?.values?.['p(99)']?.toFixed(2)  || 'N/A';
  const sP50  = data.metrics.spring_duration?.values?.['p(50)']?.toFixed(2)   || 'N/A';
  const sP95  = data.metrics.spring_duration?.values?.['p(95)']?.toFixed(2)   || 'N/A';
  const sP99  = data.metrics.spring_duration?.values?.['p(99)']?.toFixed(2)   || 'N/A';

  return {
    stdout: `
╔══════════════════════════════════════════════════════╗
║         QUARKUS vs SPRING BOOT — RESULTADOS          ║
╠══════════════════════════════════════════════════════╣
║ Métrica          │   Quarkus    │   Spring Boot       ║
╠══════════════════════════════════════════════════════╣
║ P50 (mediana)    │  ${qP50.padStart(8)}ms  │  ${sP50.padStart(8)}ms         ║
║ P95              │  ${qP95.padStart(8)}ms  │  ${sP95.padStart(8)}ms         ║
║ P99              │  ${qP99.padStart(8)}ms  │  ${sP99.padStart(8)}ms         ║
╚══════════════════════════════════════════════════════╝
`,
  };
}
