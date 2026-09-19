import http from 'k6/http';
import { check } from 'k6';

// ============================================================
// TEST DE STARTUP: Mide cuánto tarda cada servicio en responder
// desde que el pod arranca — ejecutar justo después de deploy
// Uso: k6 run benchmark/k6/startup-test.js
// ============================================================

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  const services = [
    { name: 'Quarkus JVM',    url: __ENV.QUARKUS_URL    || 'http://localhost:8080' },
    { name: 'Spring Boot',    url: __ENV.SPRING_URL     || 'http://localhost:8090' },
  ];

  for (const service of services) {
    const start = Date.now();
    let ready = false;
    let attempts = 0;

    while (!ready && attempts < 60) {
      try {
        const res = http.get(`${service.url}/q/health/ready`, { timeout: '2s' });
        if (res.status === 200) {
          ready = true;
          const elapsed = Date.now() - start;
          console.log(`✅ ${service.name}: Ready in ${elapsed}ms`);
        }
      } catch (_) {
        // Aún no responde
      }
      attempts++;
      if (!ready) {
        import { sleep } from 'k6'; // eslint-disable-line
        // k6 no tiene sleep async real aquí — usamos el loop
      }
    }

    if (!ready) console.log(`❌ ${service.name}: Did not start within 60s`);
  }
}
