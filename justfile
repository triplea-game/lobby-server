# lobby-server — gradle build, local dev (Quarkus Dev Services / docker compose),
# and prod deploy.
#
# SSH_USER selects the deploy ssh user (defaults to $USER).

set shell := ["bash", "-euo", "pipefail", "-c"]

ssh_user := env_var_or_default("SSH_USER", env_var_or_default("USER", ""))

# Matches %dev.quarkus.datasource.devservices.db-name in application.properties.
dev_db := "lobby_db"

alias test := check
alias run := up

# Show available recipes.
default:
    @just --list

# Install pre-commit as a pre-push git hook and enable testcontainers reuse.
setup:
    #!/usr/bin/env bash
    uv tool install pre-commit
    pre-commit install --hook-type pre-push
    if ! grep -qs '^testcontainers.reuse.enable=true' "${HOME}/.testcontainers.properties"; then
      echo 'testcontainers.reuse.enable=true' >> "${HOME}/.testcontainers.properties"
      echo "Enabled testcontainers reuse in ~/.testcontainers.properties"
    fi

# Run branch verification (tests, no formatting) — the CI gate.
check:
    ./gradlew check

# Format sources in place (Google Java Format via Spotless).
format:
    ./gradlew spotlessApply

# Wipe local state: the Dev Services database, the compose stack's volumes, and build artifacts.
clean:
    #!/usr/bin/env bash
    set -euo pipefail
    for id in $(just _dev-db); do docker rm -f -v "$id"; done
    docker compose down -v
    ./gradlew clean

# Connect to the Quarkus Dev Services Postgres started by `just up`.
psql:
    docker exec -it "$(just _dev-db | head -n 1)" psql -U quarkus {{dev_db}}

# Build against a local '../triplea' client checkout.
build-with-libs:
    ./gradlew --include-build ../triplea compileJava

# Run a local lobby; Quarkus Dev Services starts Postgres automatically.
up:
    ./gradlew quarkusDev

# Build (skipping tests + spotless) and bring the stack up via docker compose.
compose-up:
    ./gradlew build -x test -x testInteg -x spotlessCheck && docker compose up --build

# Deploy an image tag to prod (CI passes sha-<commit>).
deploy tag="latest":
    ANSIBLE_CONFIG="deploy/ansible.cfg" ansible-playbook -e ansible_user={{ssh_user}} -e lobby_tag={{tag}} --inventory deploy/ansible/inventory.linode.yml deploy/ansible/playbook.yml

# Print the ids of this repo's Dev Services Postgres containers, running or stopped.
_dev-db:
    #!/usr/bin/env bash
    set -euo pipefail
    for id in $(docker ps -aq --filter label=io.quarkus.devservice.launch-mode=DEVELOPMENT); do
      if docker inspect "$id" | grep -q '"POSTGRES_DB={{dev_db}}"'; then echo "$id"; fi
    done
