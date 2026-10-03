Use `just format` then `just check` to validate the project

## Tech Stack
- Java 21, Quarkus (RESTEasy Classic, WebSockets), JDBI (no ORM), Postgres, Flyway, Lombok
- Gradle build; the Quarkus fast-jar (`build/quarkus-app/`) is the deployment artifact
- Quarkus Dev Services (Testcontainers) start Postgres for dev mode and tests

## Common Commands
- `just check` — run all tests without formatting (used in CI)
- `just format` — format code only (Google Java Format)
- `just up` — run a local lobby in Quarkus dev mode; Dev Services starts Postgres
- `just compose-up` — start a local lobby server + database via docker-compose (port 3000)

## Testing
- `src/test/` — unit tests; no database or server required
- `src/testInteg/` — integration tests; require Docker (Dev Services starts a live Postgres)
- Do not put integration tests in `src/test/` or unit tests in `src/testInteg/`

## Code Style
- Google Java Format is enforced via Spotless (`just format`)
- No wildcard imports (Spotless removes them)
- Use Lombok annotations (`@Value`, `@Builder`, `@Data`, `@RequiredArgsConstructor`, etc.) instead of hand-written boilerplate

## Database
- No ORM — use JDBI with SQL object pattern
- Database migrations go in `src/main/resources/db/migration/` as Flyway `.sql` files
- Migration naming convention: `V{major}.{minor}.{patch}__description.sql`
