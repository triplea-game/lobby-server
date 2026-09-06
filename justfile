# lobby-server — gradle build, local dev (Quarkus Dev Services / docker compose),
# and prod deploy.
#
# SSH_USER selects the deploy ssh user (defaults to $USER).

set shell := ["bash", "-euo", "pipefail", "-c"]

ssh_user := env_var_or_default("SSH_USER", env_var_or_default("USER", ""))

alias test := check

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

clean:
    ./gradlew clean

# Auto-format then verify — the recommended developer loop.
verify:
    ./gradlew spotlessApply check

# Connect to the local Postgres dev container (whatever publishes 5432).
connect-to-database:
    #!/usr/bin/env bash
    id="$(docker ps --filter publish=5432 --filter status=running -q | head -n1)"
    docker exec -it --user postgres "$id" psql lobby_db

# Build against a local '../triplea' client checkout.
build-with-libs:
    ./gradlew --include-build ../triplea compileJava

# Run a local lobby; Quarkus Dev Services starts Postgres automatically.
run:
    ./gradlew quarkusDev

# Build (skipping tests + spotless) and bring the stack up via docker compose.
compose:
    ./gradlew build -x test -x testInteg -x spotlessCheck && docker compose up --build

# Trigger prod to pull the latest docker image and restart services.
deploy:
    ANSIBLE_CONFIG="deploy/ansible.cfg" ansible-playbook -e ansible_user={{ssh_user}} --inventory deploy/ansible/inventory.linode.yml deploy/ansible/playbook.yml
