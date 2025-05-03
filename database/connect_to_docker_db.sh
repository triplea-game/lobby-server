#!/bin/bash

# Simple helper script to connect to a DB running locally on docker.
docker compose exec database psql -U postgres

# after connecting, use "\l" to print the list of database
# Use "\c <database_name>" to connect to a database
# After connecting to a database, use "\d" to list the tables in the current database
