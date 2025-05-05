#!/bin/bash

# This script is a wrapper around 'ansible-playbook'
# When run without any args, ansible is run in 'dry-run' mode and will not make any changes.
# Any args passed into this script are passed directly to the ansible-playbook command.
#
# Examples: ./run.sh --limit bot --tags system --verbose


set -eu

scriptDir="$(dirname "$0")"

function installAnsible() {
  read -p "Ansible not installed, would you like to install it now with apt (y/n)? " -n 1 -r
  echo    # (optional) move to a new line
  if [[ $REPLY =~ ^[Yy]$ ]]
  then
    # Install steps from:
    # https://docs.ansible.com/ansible/latest/installation_guide/installation_distros.html
    sudo apt update
    sudo apt install software-properties-common
    sudo add-apt-repository --yes --update ppa:ansible/ansible
    sudo apt install --yes ansible
  else
    exit
  fi
}

function printCheckMode() {
  echo ""
  echo "!!! PREVIEW MODE, NO CHANGES ARE ACTUALLY MADE !!!"
  echo "    To apply changes, instead run: APPLY=1 $0"
}

# Check if ansible is installed, if not, then ask to install it.
hash ansible-playbook 2> /dev/null || installAnsible

if [[ "${APPLY-}" == "1" ]]; then
  CHECK_MODE=""
else
  CHECK_MODE="--check --diff"
  printCheckMode
fi

if [[ "${TRIPLEA_ANSIBLE_VAULT_PASSWORD-}" == "" ]]; then
  echo "No vault password variable was set. Required. Set env variable: TRIPLEA_ANSIBLE_VAULT_PASSWORD"
  exit 1
else
  echo "$TRIPLEA_ANSIBLE_VAULT_PASSWORD" > "$scriptDir/vault-password";
  trap 'rm -f "$scriptDir/vault-password"' EXIT
fi

set -x
# run ansible
# shellcheck disable=SC2068
ANSIBLE_CONFIG="$scriptDir/ansible.cfg" \
  ansible-playbook $@ \
    --vault-password-file "$scriptDir/vault-password" \
    --inventory "$scriptDir/ansible/prod.inventory" \
    $CHECK_MODE "$scriptDir/ansible/playbook.yml"
set +x

if [[ "${APPLY-}" != "1" ]]; then
  printCheckMode
fi

