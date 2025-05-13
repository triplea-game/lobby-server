#!/bin/bash

# This is an alternative script to 'run.sh' that starts a local
# database only. 'run.sh' also starts up the lobby too,
# this script is database only.

POSTGRES_PORT=5432 ./gradlew databaseComposeUp


