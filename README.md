# lobby-server

## Repository Build Artifacts

(1) Docker Container for lobby server application. This runs the lobby.

(2) Docker Container for flyway migration. This updates database, should
be run before lobby.



## Building


### Configure Github Access Token (one-time)

[Create a personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic)

Create file (or append to):  `~/.gradle/gradle.properties`, the following:

```
triplea.github.username=CHANGE_ME
triplea.github.access.token=CHANGE_ME
```

## Building

Runs all validations (tests/linters) and runs code auto-formatter:

```
./verify.sh
```

## Running (local dev environment via docker compose)

Building from source and running locally:

```
./gradlew shadowJar
docker compose up

(TODO: add env variable so we can do something like: `LOBBY_PORT=8080 docker compose up`)
```

After the compose up, a database will be started, flyway migrations on the local
development environment will be run against that database. Finally, the lobby server
application is started and will use that same database.

### Checking running environment

```
$ docker container ls

CONTAINER ID   IMAGE                COMMAND                  CREATED      STATUS                   PORTS                                         NAMES
2eb8f441511f   lobby-server-lobby   "/bin/sh -c 'java -j…"   5 days ago   Up 9 seconds             0.0.0.0:32769->8080/tcp, :::32769->8080/tcp   lobby-server-lobby-1
a395fabccd4e   postgres:10          "docker-entrypoint.s…"   5 days ago   Up 9 seconds (healthy)   0.0.0.0:5432->5432/tcp, :::5432->5432/tcp     lobby-server-database-1
```


### Connecting to local database

```
docker exec -it --user postgres lobby-server-database-1 psql
```


## Running (prod environment with docker)

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



## Running DB Migrations

In a development environment, DB migrations will be run as part of `docker compose up`

### Prod

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

## Prod - useful commands


```
sudo systemctl status lobby-2.6.service
sudo systemctl restart lobby-2.6.service

tail -f /var/log/lobby-2.6.log

docker container ls
docker logs [lobby container name]
```


