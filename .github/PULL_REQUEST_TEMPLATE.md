# Pull Request Template

## Description
_Please include a concise summary of the changes introduced and the rationale behind them._

**Related issue:** Closes #[issue number]

---

## Validation Checklist

- [ ] **Builds successfully**: Project compiles without errors (`./gradlew build`).
- [ ] **All unit tests pass**: Ensure there are no regressions in business logic.
- [ ] **Test coverage ≥ 80%**
- [ ] **Integration tests pass**: Tests covering JPA, security, and full endpoint flows.
- [ ] **Acceptance criteria met**: All acceptance criteria for the related issue(s) have been implemented and verified.
- [ ] **No lint/static analysis warnings**: Ktlint or Detekt report no new issues.
- [ ] **Security validated**: Spring Security tests (roles/permissions, JWT) executed successfully.
- [ ] **API documentation updated**: Swagger reflects new or modified endpoints.
- [ ] **Actuator and metrics configured**: Health/metrics/tracing endpoints are accessible.
- [ ] **Environment variables documented**: README or `.env.example` includes new configuration properties.
- [ ] **Docker Compose verified**: Application (app + Postgres) starts without errors.
- [ ] **README/CHANGELOG updated**: Describes changes and testing instructions.
- [ ] **Peer review completed**: Feedback from at least one teammate has been incorporated.

---

## How to Test the Changes
_Provide brief steps to set up the environment and verify the introduced changes._

1. `git checkout -b feature/your-feature`
2. `./gradlew build && ./gradlew test`
3. `docker-compose up --build`
4. Visit `http://localhost:8080/actuator/health`

---

> Thank you for your contribution! Please ensure all items above are complete before requesting a review.

