# lobby-server


## Building


### Before Starting

Need to set gradle properties:
```
triplea.github.username
triplea.github.access.token
```

Create file (or append to):  `~/.gradle/gradle.properties`, the following:

```
triplea.github.username=CHANGE_ME
triplea.github.access.token=CHANGE_ME
```


## Building

```
./gradlew check
```

## Running 

TODO


## CI / CD  - What happens when master is updated

- code compilation checks
- docker image of the lobby published to github docker registry





