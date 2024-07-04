# lobby-server


## Building


### Before Starting

Create file (or append to):  `~/.gradle/gradle.properties`, the following:

```
triplea.github.username=CHANGE_ME
triplea.github.access.token=CHANGE_ME
```


## Building

```
./verify.sh
```

## Running 

```
docker compose up
```

Lobby will be running on localhost with a random port number

(TODO: add env variable so we can do something like: `LOBBY_PORT=8080 docker compose up`)



