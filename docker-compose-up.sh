#!/bin/bash
./gradlew shadowJar
LOBBY_PORT=3000 docker compose up

