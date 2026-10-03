Use `just format` then `just check` to validate the project

## Tech Stack
- Java 21, DropWizard (HTTP server), JDBI (no ORM), Postgres, Lombok
- Gradle build with shadow JAR as deployment artifact
- Docker + docker-compose for local dev and integration tests

## Common Commands
- `just check` — run all tests without formatting (used in CI)
- `just format` — format code only (Google Java Format)
- `just up` — run a local lobby in Quarkus dev mode; Dev Services starts Postgres
- `just compose-up` — start a local lobby server + database via docker-compose (port 3000)

## Testing
- `src/test/` — unit tests; no database or server required
- `src/testInteg/` — integration tests; require Docker (docker-compose spins up a live DB and server automatically via Gradle)
- Do not put integration tests in `src/test/` or unit tests in `src/testInteg/`

## Code Style
- Google Java Format is enforced via Spotless (`just format`)
- No wildcard imports (Spotless removes them)
- Use Lombok annotations (`@Value`, `@Builder`, `@Data`, `@RequiredArgsConstructor`, etc.) instead of hand-written boilerplate

## Database
- No ORM — use JDBI with SQL object pattern
- Database migrations go in `src/main/resources/db/migration/` as Flyway `.sql` files
- Migration naming convention: `V{major}.{minor}.{patch}__description.sql`
