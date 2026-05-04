MAKEFLAGS += --always-make --warn-undefined-variables
SHELL=/bin/bash -ue

SSH_USER ?= $${USER}

help: ## Show this help text
	grep -h -E '^[a-z]+.*:' $(MAKEFILE_LIST) | \
		awk -F ":|#+" '{printf "\033[31m%s $(nc) \n   %s $(nc)\n    \033[3;37mDepends On: $(nc) [ %s ]\n", $$1, $$3, $$2}'


setup: ## Installs pre-commit as a pre-push git hook (requires pre-commit to be installed)
	uv tool install pre-commit
	pre-commit install --hook-type pre-push

check test: ## run branch verification
	./gradlew check

format: ## Runs formatting
	./gradlew spotlessApply

clean:
	./gradlew clean

verify: ## useful for developers, automatically format
	./gradlew spotlessApply check

connect-to-database: ## Dev utility command to connect to a local database
	# Look for any containers publishing port 5432, the port we expect postgres to be using
	# Run 'psql' as 'postgres' user on the DB container
	dbContainerName=$(shell docker ps --filter publish=5432 --filter status=running --format {{.Names}}) \
	&& docker exec -it --user postgres "$dbContainerName" psql lobby_db

build-with-libs: ## Build with local 'triplea' client
	./gradlew --include-build ../triplea compileJava

run: ## Runs a local lobby (with database) — Quarkus Dev Services starts Postgres automatically
	./gradlew quarkusDev

deploy-prod: ## Triggers prod to pull latest docker, run flyway and restart services
	ANSIBLE_CONFIG="deploy/ansible.cfg" \
	  ansible-playbook \
	    -e ansible_user=$(SSH_USER) \
	    --inventory deploy/ansible/linode.inventory.yml \
	    deploy/ansible/playbook.yml
