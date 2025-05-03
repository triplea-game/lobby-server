# Database

Hosts database migrations. Migration files are raw SQL, they are packaged onto
a 'flyway' docker, flyway is a tool to run those SQL files.


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

