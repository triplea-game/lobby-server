Use `make verify` to validate the project

## Tech Stack
- Java 21, DropWizard (HTTP server), JDBI (no ORM), Postgres, Lombok
- Gradle build with shadow JAR as deployment artifact
- Docker + docker-compose for local dev and integration tests

## Testing
- `src/test/` — unit tests; no database or server required
- `src/testInteg/` — integration tests; require Docker (docker-compose spins up a live DB and server automatically via Gradle)
- Do not put integration tests in `src/test/` or unit tests in `src/testInteg/`

## Code Style
- Google Java Format is enforced via Spotless (`make format` or `./gradlew spotlessApply`)
- No wildcard imports (Spotless removes them)
- Use Lombok annotations (`@Value`, `@Builder`, `@Data`, `@RequiredArgsConstructor`, etc.) instead of hand-written boilerplate

## Database
- No ORM — use JDBI with SQL object pattern
- Database migrations go in `database/migrations/` as Flyway `.sql` files
- Migration naming convention: `V{major}.{minor}.{patch}__description.sql`
