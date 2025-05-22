# Libra Wallet

Una plataforma **end-to-end** (web y mobile) para gestionar y monitorear una billetera electrónica similar a PayPal o MercadoPago. Permite a los usuarios crear y operar con sus cuentas virtuales, seguir su historial de movimientos y realizar transferencias de manera segura e intuitiva.

---

## Funcionalidades principales

1. **Gestión de cuenta virtual**
    - Crear cuenta asociada a email y contraseña.
    - Consultar saldo disponible.
    - Registrar y visualizar historial simplificado de transacciones:
        - Gastos
        - Ingresos de dinero

2. **Transferencias P2P**
    - Enviar dinero a otro usuario mediante email o ID único.
    - Registro de la transferencia en el historial de ambos usuarios. 

3. **Integración de medios externos (simulada)**
    - Recarga de saldo “como si” fuera una transferencia desde tarjeta o cuenta bancaria.
    - Simulación de DEBIN: solicitud desde el front-end y cobro a medio externo.

4. **Interfaz minimalista**
    - Web y mobile con diseño ligero, enfocado en lo esencial para pruebas de QA.

---

## Stack tecnológico

- **Backend:** Kotlin + Spring Boot
- **API REST:** Spring MVC / Tomcat embebido
- **Persistencia:** Spring Data JPA + PostgreSQL (H2 en tests)
- **Seguridad:** Spring Security (JWT o Basic Auth)
- **Validación:** Bean Validation (Hibernate Validator)
- **Observabilidad:** Spring Boot Actuator
- **Contenedores:** Docker Compose con volúmenes persistentes

---

## QA & Pruebas

- **Unitarios**: xUnit / JUnit + Specs
- **Integración full-stack**: tests contra persistencia y APIs internas/externas
- **End-to-end**: Cypress (web headless) y Appium (mobile)
- **Stress / Load testing**: Locust (Python), con estrategia diferenciada para cada esquema

---

## CI/CD & Versionado

- Versionado SemVer en GitHub
- Pipelines en GitHub Actions (build, tests, delivery vía Docker)
- Entrega final: Docker Compose up
- Presentación y defensa del TP en junio 2025

---

¡Bienvenido a Libra Wallet! Para más detalles, consulta la **documentación interna** y el **roadmap de QA** en el repositorio.

> Nota para desarrolladores:
> Previo a iniciar con el proyecto, asegúrate de tener el `commit_message` pre-seteado, usando el siguiente comando:
```bash
git config commit.template .gitmessage
 ```