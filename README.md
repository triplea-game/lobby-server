# lobby-server


## Building


### Before Starting

Need to set gradle properties:
```
triplea.github.username
triplea.github.access.token
```

Use the script below to create (or append to) the file: `~/.gradle/gradle.properties`

```
GITHUB_USER=[change-me]
ACCESS_TOKEN=[change-me]


mkdir -p ~/.gradle
echo "triplea.github.username=$GITHUB_USER
triplea.github.access.token=$ACCESS_TOKEN" >> ~/.gradle/gradle.properties
```



## Building

```
./gradlew check
```

## Running 

TODO


