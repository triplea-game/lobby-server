# lobby-server — gradle build, local dev (Quarkus Dev Services / docker compose),
# and prod deploy.
#
# SSH_USER selects the deploy ssh user (defaults to $USER).

set shell := ["bash", "-euo", "pipefail", "-c"]

ssh_user := env_var_or_default("SSH_USER", env_var_or_default("USER", ""))

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

clean:
    ./gradlew clean

# Connect to the local Postgres dev container (whatever publishes 5432).
psql:
    #!/usr/bin/env bash
    id="$(docker ps --filter publish=5432 --filter status=running -q | head -n1)"
    docker exec -it --user postgres "$id" psql lobby_db

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
