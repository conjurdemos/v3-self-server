#!/bin/bash

source ./cybrtest.config

export CURL="curl -s"
export BASE_URL=http://localhost:8080/cybr
export DEBUG=true

main() {
  appDbResponse=$($CURL -H "Content-Type: application/json" "$BASE_URL/appgovdb?filter=unapproved")
  test "unapproved" "$appDbResponse"

  # Login as admin to PAS
  PASauthnCreds=$(echo "$PAS_ADMIN_NAME:$PAS_ADMIN_PASSWORD" | base64)
  pasAuthnResponse=$($CURL -H "Authorization: Basic $PASauthnCreds" -H "Content-Type: application/json" "$BASE_URL/pas/login")
  test "paslogin" "$pasAuthnResponse"

  # Login as admin to Conjur
  ConjurAuthnCreds=$(echo "$CONJUR_ADMIN_NAME:$CONJUR_ADMIN_PASSWORD" | base64)
  conjurAuthnResponse=$($CURL -H "Authorization: Basic $ConjurAuthnCreds" -H "Content-Type: application/json" "$BASE_URL/conjur/login")
  test "conjurlogin" "$conjurAuthnResponse"
}

###################################
test() {
  if ! $DEBUG; then return; fi
  local funcname=$1; shift
  local value="$(echo $1 | tr -d '\r\n')"; shift

  if [[ $value == null ]]; then
    echo "$funcname returned null."
  else
    echo "$funcname returned:"
    echo "$value"
    echo
  fi
}

main "$@"
