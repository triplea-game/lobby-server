# lobby-server


## Lobby Server

### Background

Lobby-Server is a 'new' server to host lobby and other functionalities. Historically this was
powered by a pure java stack that used java sockets (NIO). The java server was written very early
in the project, mid-2000s, the 'http-server' allows for a modern (2019) server to be used.
The modern server has integration with JDBI, annotation based rate limiting, authentication and
affords an opportunity to rewrite the lobby server in a simpler and more modular fashion.

### Authentication

Connecting to the server, on success, will issue an API key back to the client. Subsequent
interactions from the client  with the server will send the API key to server for further
authorization. Keep in mind all endpoints are publicly available.

### Communication Directions - Http to Server & Websocket to Client

Communication to server is done via standard Http endpoints. Server will process these messages
triggering event listeners that will communicate back to clients via websocket.

### Keep-Alive

This concept is to avoid 'ghosts' when we fail to process a disconnect. Players and connected games
will need to send HTTP requests to a keep-alive endpoint to explicitly 'register' their liveness. When
these messages are not received after a cut-off period, then the game or player are removed.



## CI/CD Build Flow

### Build Artifacts

(1) Docker Container for lobby server application. This runs the lobby.

(2) Docker Container for flyway migration. This updates database.


### Build Actions

When master branch is updated:
- build push docker images to github packages
  - server image
  - "flyway" image with DB migrations
- update prod to latest version, zero downtime deployment, run ansible:
    - ensure postgres is running on docker
    - fetch docker flyway image and run it against postgres
    - deploy a 'blue' environment lobby server
    - switch NGINX to 'blue' environment
    - stop & update 'green' environment
    - switch back to 'green' environment
    - turn off the 'blue' environment


## Development


### Configure Github Access Token (one-time)

[Create a personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic)

The access token needs: public_repo, read:packages

Create file (or append to):  `~/.gradle/gradle.properties`, the following:

```
triplea.github.username=CHANGE_ME
triplea.github.access.token=CHANGE_ME
```

### Building

Runs all validations (tests/linters) and runs code auto-formatter:

```
./verify.sh
```

Docker compose is used to start a database during build.
That database is left running so that future builds can be faster
and skip the DB setup, and to let developers to look at the database
state after tests.



### Running (local dev environment via docker compose)

Building from source and running locally:

```
./docker-compose-up.sh
```

After the compose up, lobby will be running on port 3000, database will be started
as well and latest flyway migrations run.

After this, launch the game client, go to settings, update settings to use lobby
running on http://localhost:3000


#### Debugging compose up failures

Run the docker compose up command directly via CLI and look for errors:

```
docker compose  -p lobby up
```


### Checking running environment

```
$ docker container ls

CONTAINER ID   IMAGE                COMMAND                  CREATED      STATUS                   PORTS                                         NAMES
2eb8f441511f   lobby-server-lobby   "/bin/sh -c 'java -j…"   5 days ago   Up 9 seconds             :::3000->8080/tcp, :::32769->8080/tcp   lobby-server-lobby-1
a395fabccd4e   postgres:10          "docker-entrypoint.s…"   5 days ago   Up 9 seconds (healthy)   0.0.0.0:5432->5432/tcp, :::5432->5432/tcp     lobby-server-database-1
```


### Connecting to local database

```
docker exec -it --user postgres lobby-server-database-1 psql
```

### Rebuild from clean

```
docker compose rm -f
./run.sh
```

### Running (prod-like environment with docker)

```
set +o history
DB_PASS=...
set -o history

docker pull ghcr.io/triplea-game/lobby:latest
docker run   \
  --network host   \
  -e HTTP_PORT="8026"   \
  -e DB_URL="localhost:5432/lobby_db"   \
  -e DATABASE_USER="lobby_user"   \
  -e DATABASE_PASSWORD="$DB_PASS"   \
  ghcr.io/triplea-game/lobby
```


## Production

### Updating Prod Lobby version

Restart the lobby service: ```sudo systemctl restart lobby-2.6```

That will shut down the current lobby, pull the latest docker container and start it up.


## Prod - useful commands


```
sudo systemctl status lobby-2.6.service
sudo systemctl restart lobby-2.6.service

tail -f /var/log/lobby-2.6.log

docker container ls
docker logs [lobby container name]
```



Run flyway via docker container:

```
ssh prod.triplea-game.org

set +o history
DB_PASS=...
set -o history

docker run \
  --network=host \
  ghcr.io/triplea-game/lobby/flyway \
    -locations=filesystem:/flyway/sql \
    -connectRetries=60 \
    -user=lobby_user \
    -password=$DB_PASS \
    -url=jdbc:postgresql://localhost:5432/lobby_db \
    migrate
```



# Design Notes

## API Keys for Moderators

HTTP endpoints are publicly available, they can be found and attacked.
Any endpoint that initiates a moderator action will accept
headers for a moderator API key and a password for that key.

Any such endpoints that take moderator keys should verify
the keys and lock out IP addresses that make too many attempts.

### Key Distribution

Super-moderators can elevate users to moderator and generate
a 'single-use-key'. This key is then provided to that moderator.

The moderator can then 'register' the key, where they provide
the single-use-key and a password. The backend verifies
the single-use-key and then generates a new key. This is
done so that only the moderator will then 'know' the value
of their new key. The password provided is used as a salt.

### API Key Password

The purpose behind this is so that if an API key is compromised,
it won't be useful unless the password is also compromised too.

This means a moderator will need to have their OS data to
be hacked and also a key logger or something that can scrape
the password from in-memory of TripleA.

The API key password is stored in-memory when TripleA launches
and shall not be persisted anyways.

API keys are stored in "client settings", which are stored
with the OS and persist across TripleA installations.

## API Key Rate Limiting

Rate-limiting: of note, the backend implementation should be careful to apply rate limiting
to any/all endpoints that take an API key so as to avoid brute-force attacks to try and crack
an API key value.

# WARNINGS!

## Multiple SLF4J Bindings Not Allowed

Dropwizard uses Logback and has a binding with SLF4J baked in. Additional SLF4J bindings
should generate a warning, but will ultimately cause problems (when run from gradle) and drop
wizard may fail to start with this error:

```bash
java.lang.IllegalStateException: Unable to acquire the logger context
    at io.dropwizard.logging.LoggingUtil.getLoggerContext(LoggingUtil.java:46)
```

## Stream is already closed

This can happen when the server side does not fail fast on incorrect input.
This can be if we use headers that are missing or do not check that parameters are present.

The stack trace indicating this will look like this:
```bash
ERROR [2019-06-06 05:07:22,247] org.glassfish.jersey.server.ServerRuntime$Responder: An I/O error has occurred while writing a response message entity to the container output stream.
! java.lang.IllegalStateException: The output stream has already been closed.
```

The impact of this is:
- server thread hangs
- client hangs
- server does not shutdown cleanly

This is bad as it could be used in a DDOS attack.

### Prevention

Essentially fail-fast:
- When looking for headers, verify headers exist or terminate the request
- Verify that all needed GET parameters are present or terminate the request

To terminate the request, just throw a IllegalArgumentException, it'l be mapped to a 400.

## 404 error, but endpoint is registered!?

Make sure in addition to the `@Path` annotation on the endpoint method,
ensure the controller class has a `@Path("")` annotation on it.

### Design Pattern for Transactions

- Create a new interface; e.g.: `ModeratorKeyRegistrationDao.java`
- Add a default method with the `@Transaction` annotation.
- add a dummy select query so that JDBI sees the interface as valid
- pass the needed DAO objects as parameters to the default method
- use mockito mocks to test the method


