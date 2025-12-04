MAKEFLAGS += --always-make --warn-undefined-variables
SHELL=/bin/bash -ue

ansible-galaxy-install:
	ansible-galaxy collection install -r deploy/ansible/requirements.yml --force

vaultPassword=@echo "${TRIPLEA_ANSIBLE_VAULT_PASSWORD}" > deploy/vault-password; trap 'rm -f "deploy/vault-password"' EXIT
runAnsible=ANSIBLE_CONFIG="deploy/ansible.cfg" ansible-playbook --vault-password-file deploy/vault-password
testInventory=--inventory deploy/ansible/test.inventory
prodInventory=--inventory deploy/ansible/prod.inventory
playbook=deploy/ansible/playbook.yml

diff-test: ansible-galaxy-install
	$(vaultPassword); \
	$(runAnsible) \
		--diff \
		--check \
		$(testInventory) \
		$(playbook)

deploy-test: ansible-galaxy-install
	$(vaultPassword); \
	$(runAnsible) \
		--diff \
		$(testInventory) \
		$(playbook)

run:
	POSTGRES_PORT=5432 LOBBY_PORT=3000 ./gradlew composeUp
