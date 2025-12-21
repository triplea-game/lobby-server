MAKEFLAGS += --always-make --warn-undefined-variables
SHELL=/bin/bash -ue

SSH_USER ?= $${USER}

clean:
	./gradlew composeDown clean
	docker compose rm -f

check: ## run branch verification
	./gradlew check

verify: ## useful for developers, automatically format
	./gradlew spotlessApply check


ansible-galaxy-install: ## install ansible collections from TripleA
	ansible-galaxy collection install -r deploy/ansible/requirements.yml --force

vaultPassword=@echo "${TRIPLEA_ANSIBLE_VAULT_PASSWORD}" > deploy/vault-password; trap 'rm -f "deploy/vault-password"' EXIT
runAnsible=ANSIBLE_CONFIG="deploy/ansible.cfg" ansible-playbook --vault-password-file deploy/vault-password  -e ansible_user=$(SSH_USER)
testInventory=--inventory deploy/ansible/test.inventory
prodInventory=--inventory deploy/ansible/prod.inventory
playbook=deploy/ansible/playbook.yml

diff-test: ansible-galaxy-install ## Does a deployment "diff" against test, does not make changes
	$(vaultPassword); \
	$(runAnsible) \
		--diff \
		--check \
		$(testInventory) \
		$(playbook)

deploy-test: ansible-galaxy-install ## deploys to 'test'
	$(vaultPassword); \
	$(runAnsible) \
		--diff \
		$(testInventory) \
		$(playbook)

connect-to-database: ## Dev utility command to connect to a local database
	# Look for any containers publishing port 5432, the port we expect postgres to be using
	# Run 'psql' as 'postgres' user on the DB container
	dbContainerName=$(shell docker ps --filter publish=5432 --filter status=running --format {{.Names}}) \
	&& docker exec -it --user postgres "$dbContainerName" psql lobby_db

build-with-libs: ## Build with local 'triplea' client
	./gradlew --include-build ../triplea compileJava

run: ## Runs a local lobby (with database)
	POSTGRES_PORT=5432 LOBBY_PORT=3000 ./gradlew composeUp
