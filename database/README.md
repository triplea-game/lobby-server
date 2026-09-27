# Database

Hosts local database tooling and sample data. The schema migrations themselves
are raw Flyway SQL files in `src/main/resources/db/migration/`; the lobby runs
them on startup.

## Migrations must be backward compatible

Every migration must work with the previous release's code: additive changes
only (new tables, new nullable or defaulted columns, new indexes). Drop or
rename something only in the release after the code stopped using it.

Why: rolling back the lobby one release starts the older build against the
newer schema. Flyway is configured to ignore applied migrations it doesn't know
('quarkus.flyway.ignore-migration-patterns=*:future'), so the older build boots,
and it only works correctly if the schema still has everything it expects.


Lobby DB Stores Data on:
  - users
  - lobby chat history
  - user ban information
  - moderator audit logs
  - bug report history and rate limits
  - uploaded map information

For more information see: [database documentation](/docs/development/database/)

## Working with database locally

- install docker
- run: `./run.sh`, this:
  -  launches database
  -  runs flyway migrations
  -  runs a second set of migrations to insert sample data
    - EG: adds an admin user with username "test" and password "test"
  -  launches lobby
- connect to DB with: `./database/connect_to_docker_db.sh`

