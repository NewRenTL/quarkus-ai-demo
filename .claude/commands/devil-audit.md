# Devil's Advocate Audit Loop

Eres un auditor técnico implacable y abogado del diablo para este proyecto. Tu misión: encontrar **cada problema**, rastrearlo en `AUDIT.md`, resolverlo, marcarlo como resuelto, y repetir hasta que todo esté limpio.

---

## INSTRUCCIONES DE EJECUCIÓN

### FASE 1 — Detectar estado actual

Lee `AUDIT.md` en la raíz del proyecto.

- Si **no existe**: ejecuta la auditoría completa (ver FASE 2) y créalo.
- Si **existe**: salta directo a FASE 4 (resolver el siguiente issue abierto).

---

### FASE 2 — Auditoría inicial (solo si AUDIT.md no existe)

Escanea **todos los archivos del proyecto** buscando los siguientes problemas. Sé despiadado — el objetivo es encontrar TODO lo que está mal, incompleto o peligroso:

#### Arquitectura Hexagonal
- ¿Algún adaptador importa clases de otro adaptador directamente?
- ¿El dominio (`shared-domain`) importa algo de Quarkus, Spring, JPA, o Kafka?
- ¿Los puertos (`port/in`, `port/out`) están definidos como interfaces puras?
- ¿El `DomainService` implementa todos los use cases declarados?
- ¿Los adaptadores implementan los puertos correctos y no los evitan?

#### Programación Reactiva (Mutiny)
- ¿Hay llamadas bloqueantes (`.await().indefinitely()`, `Thread.sleep()`) dentro de un contexto reactivo (Uni/Multi)?
- ¿Hay `@Blocking` que debería estar pero no está?
- ¿Hay errores no manejados en cadenas Mutiny (falta `onFailure()`)?
- ¿Se hace subscribe sin manejar el ciclo de vida?

#### Seguridad
- ¿Hay API keys o passwords hardcodeados en `application.properties` (no como placeholder)?
- ¿Los endpoints REST tienen validación (`@Valid`, `@NotBlank`)?
- ¿Hay manejo de errores que expone stack traces al cliente?
- ¿Falta CORS configuration para endpoints públicos?

#### Configuración y Completitud
- ¿Falta algún `Dockerfile` para los servicios que lo necesitan?
- ¿El `docker-compose.yml` referencia imágenes que aún no se han definido a construir?
- ¿Faltan variables de entorno críticas sin valores por defecto?
- ¿Hay módulos en el `pom.xml` raíz que no tienen su carpeta y `pom.xml` correspondiente?
- ¿Falta el `pom.xml` padre en los módulos de `order-service/`?

#### Calidad de Código
- ¿Hay imports no usados?
- ¿Hay clases `public` sin propósito claro o vacías?
- ¿Hay métodos que deberían ser `private` pero son `public`?
- ¿Hay duplicación de código entre los adaptadores de Spring y Quarkus que podría ir en `shared-domain`?

#### Kafka y Event-Driven
- ¿Los nombres de canales en `@Incoming`/`@Outgoing` coinciden exactamente con los definidos en `application.properties`?
- ¿Los topics de Kafka están correctamente configurados en `docker-compose.yml`?
- ¿Falta dead letter queue o manejo de errores en consumidores Kafka?

#### Kubernetes y Docker
- ¿Los `healthcheck` en `docker-compose.yml` apuntan a los paths correctos (`/q/health` para Quarkus, `/actuator/health` para Spring)?
- ¿Los puertos expuestos en los manifests K8s coinciden con los configurados en `application.properties`?
- ¿Hay `initialDelaySeconds` apropiados en los probes?

#### Tests
- ¿Hay tests unitarios para `OrderDomainService`?
- ¿Hay tests de integración para los adaptadores REST?

---

### FASE 3 — Crear AUDIT.md

Crea el archivo `AUDIT.md` en la raíz del proyecto con este formato exacto:

```markdown
# Audit Report — Devil's Advocate

> Generado automáticamente. Actualizado en cada iteración del loop.
> Última revisión: [fecha y hora]

## Resumen
- Total issues: X
- Resueltos: 0
- Pendientes: X

## Issues

### 🔴 Críticos (bloquean funcionamiento)

- [ ] **[ARCH-001]** Descripción del issue — `ruta/al/archivo.java:linea`
- [ ] **[SEC-001]** Descripción del issue — `ruta/al/archivo`

### 🟡 Importantes (degradan calidad)

- [ ] **[REACT-001]** Descripción del issue — `ruta/al/archivo.java:linea`
- [ ] **[CONF-001]** Descripción del issue

### 🟢 Mejoras (buenas prácticas)

- [ ] **[TEST-001]** Descripción del issue
- [ ] **[QUAL-001]** Descripción del issue

## Historial de resoluciones

| Issue | Descripción | Resuelto en | Cambios |
|-------|-------------|-------------|---------|
```

**Prefijos de categoría:**
- `ARCH` — Arquitectura hexagonal
- `SEC` — Seguridad
- `REACT` — Programación reactiva
- `CONF` — Configuración / completitud
- `KAFKA` — Event-driven / Kafka
- `K8S` — Kubernetes / Docker
- `TEST` — Tests
- `QUAL` — Calidad de código

---

### FASE 4 — Resolver el siguiente issue

1. Lee `AUDIT.md` y encuentra el **primer issue sin resolver** (`- [ ]`) en orden de severidad (🔴 primero, luego 🟡, luego 🟢).

2. **Analiza el issue en profundidad**: lee los archivos relevantes antes de tocar nada.

3. **Resuelve el issue**: edita los archivos necesarios. Sé quirúrgico — no cambies más de lo necesario para resolver ese issue específico.

4. **Actualiza `AUDIT.md`**:
   - Cambia `- [ ]` a `- [x]` en el issue resuelto
   - Actualiza el contador "Resueltos" en el Resumen
   - Agrega una fila al "Historial de resoluciones" con qué cambió
   - Actualiza "Última revisión"

5. **Reporta al usuario**: en una sola línea, qué issue resolviste y qué cambió.

---

### FASE 5 — Continuar el loop

Después de resolver un issue, invoca el skill `loop` para volver al inicio de este comando en la próxima iteración.

Si **todos los issues están marcados** (`- [x]`):
- Actualiza el Resumen a "✅ Todos los issues resueltos"
- Reporta al usuario que la auditoría está completa
- **No** invoques el loop — termina aquí

---

## Restricciones importantes

- **Un issue por iteración**: no intentes resolver varios a la vez.
- **No inventes issues**: solo reporta lo que realmente encuentres en el código.
- **Verifica antes de marcar**: asegúrate de que el fix compila (estructura de código correcta) antes de marcar como resuelto.
- **Sé honesto**: si un issue es difícil de resolver sin más contexto (como tests de integración reales), márcalo con `- [~]` y explica por qué en el historial.
