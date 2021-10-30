#!/bin/bash

source ../../conjur-ubuntu-install/conjur.config

read -p "What is the hostname of your Synchronizer server?" SYNC_HOST
export SYNC_HOST=$(echo "Sync_$SYNC_HOST")
export VAULT_NAME=DemoVault
export CONJUR_AUTHN_LOGIN=$CONJUR_ADMIN_USERNAME
export CONJUR_AUTHN_API_KEY=$CONJUR_ADMIN_PASSWORD

cat ./vault-sync-policy.template		\
  | sed -e "s#{{ VAULT_NAME }}#$VAULT_NAME#g"	\
  | sed -e "s#{{ SYNC_HOST }}#$SYNC_HOST#g"	\
  > vault-sync-policy.yml

cat vault-sync-policy.yml | $DOCKER exec -i conjur-cli conjur policy load root -
NEW_API_KEY=$($DOCKER exec conjur-cli conjur host rotate_api_key --host $SYNC_HOST)
echo
echo "Synchronizer authn creds for Conjur primary on $MASTER_PLATFORM:"
echo "  Hostname: host/$SYNC_HOST"
echo "  API key: $NEW_API_KEY"
echo "  Conjur URL: $CONJUR_APPLIANCE_URL"
echo "  Conjur Account: $CONJUR_ACCOUNT"
echo

rm ./vault-sync-policy.yml
