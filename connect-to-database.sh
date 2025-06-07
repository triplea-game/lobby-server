#!/bin/bash


# Look for any containers publishing port 5432, the port we expect postgres to be using
dbContainerName=$(docker ps --filter publish=5432 --filter status=running --format {{.Names}})

# Run 'psql' as 'postgres' user on the DB container
docker exec -it --user postgres "$dbContainerName" psql lobby_db

