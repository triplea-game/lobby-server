#!/bin/bash

set -eu
(
  set -x
  POSTGRES_PORT=5432 LOBBY_PORT=3000 ./gradlew composeUp
)

echo "LOBBY STARTED ON PORT 3000"
