# lobby-server

Use `just format` then `just check` to validate the project.

## Tech Stack
- Java 21, Quarkus (RESTEasy Classic + Jackson, WebSockets), JDBI (no ORM), Postgres, Flyway, Lombok
- Gradle build; the Quarkus fast-jar (`build/quarkus-app/`) is the deployment artifact
- Quarkus Dev Services (Testcontainers) start Postgres for dev mode and tests

## Common Commands
- `just check` — `./gradlew check`: unit tests, `testInteg`, and `spotlessCheck` (the CI gate; does not reformat)
- `just format` — `./gradlew spotlessApply` (Google Java Format)
- `just up` — run a local lobby in Quarkus dev mode on port 8080; Dev Services starts Postgres
- `just compose-up` — build (skipping tests and Spotless), then run lobby + Postgres + NGINX + sample data via docker compose; reach the lobby through NGINX at `http://localhost/lobby`
- `just psql` — psql into the `just up` Dev Services database

## Testing
- `src/test/` — unit tests; no database or server required
- `src/testInteg/` — integration tests; require Docker (Dev Services starts a disposable Postgres; `@QuarkusTest` starts Quarkus in-process; database-rider datasets live in `src/testInteg/resources/datasets/`)
- Do not put integration tests in `src/test/` or unit tests in `src/testInteg/`

## Code Style
- Google Java Format is enforced via Spotless (`just format`)
- No wildcard imports; Spotless removes unused imports
- Use Lombok annotations (`@Value`, `@Builder`, `@AllArgsConstructor(onConstructor_ = @Inject)`, `@Slf4j`, etc.) instead of hand-written boilerplate

## Database
- No ORM — JDBI fluent API: `jdbi.withHandle(handle -> handle.createQuery(...))`, mapping rows with `ConstructorMapper` / `@ColumnName`
- Migrations are Flyway `.sql` files in `src/main/resources/db/migration/`; Quarkus runs them at startup
- Migration naming: `V{major}.{minor}.{patch}__snake_case_description.sql`, minor and patch zero-padded to two digits (e.g. `V2.03.00__drop_support_server_tables.sql`)
- Never edit an applied migration (Flyway checksums)
- Every migration must be backward compatible with the previous release's code (see `database/README.md`): additive only; drop or rename in the release after the code stops using it
