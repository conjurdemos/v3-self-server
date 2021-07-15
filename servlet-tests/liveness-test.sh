#!/bin/bash

source ./cybrtest.config

export CURL="curl -s"
export BASE_URL=http://localhost:8080/cybr
export DEBUG=true

main() {
  echo
  echo "If all is configured properly you should see output similar to below:"
  echo
  echo "unapproved returned:"
  echo "{\"unapproved\": []}"
  echo
  echo "paslogin returned:"
  echo "{\"pasSessionToken\": \"ZTY1ZmRiZGUtZmQ5My00ZWI5LThlNDEtYWJiMzFiOWI2OWVjOzgyRjA4NDJENDNBRkE0QUUwRTRGOTQ0RDhCNDJFREM5NTlGRjI2QkFDRTY5NEFDN0VCM0Y2QjNERTM2QUQyQUM7MDAwMDAwMDJCMUREREVBNzJGNEYyRDIzRURCRTcxNjBFMkQ2RThGQkNBMTQ0NUVDQjU1NkUyRjY4QUFDQkJBRkExRkY5ODAyMDAwMDAwMDA7\"}"
  echo 
  echo "conjurlogin returned:"
  echo "{\"conjurAccessToken\": \"eyJwcm90ZWN0ZWQiOiJleUpoYkdjaU9pSmpiMjVxZFhJdWIzSm5MM05zYjNOcGJHOHZkaklpTENKcmFXUWlPaUkxWkRKalpXTXlaR1EyWVRCa1lqQmpZelUwTkdJeFpETm1ZbUUyWldKaE9USmlNRFkzTUdKalpUSXdPRGN4TldWak5USmpaakV5WWpSbFlUWmhNRFF3SW4wPSIsInBheWxvYWQiOiJleUp6ZFdJaU9pSmhaRzFwYmlJc0ltbGhkQ0k2TVRZeU5qSTROalU1TjMwPSIsInNpZ25hdHVyZSI6Ik5PZVVaNHN4SjQ0dWtyQTZFeFhZc2hlQnc2X18xMzJ4a0Z0Nm4wdUZYeld6aGotNGRYSGVVdkF6VmRDY0JRc0xuVzN3NzgtRTZ3Y2dnWmdyUVBKNS1vdGZVdlQxTmg5SGhnbmMtRkQ5eGpZOTdpRHZMeTBmREJIZUpUUll6NTZ5Z3BFZ1o1U0xjMU9Yb0NnNmw0N3NmeVU2Z2U1SVRmSERJWW9xZW5jak8yVlFhd0pUSVhObUhmMjIzMnRfcjRiZ3dZT1lfNHRzWjFLQnJMTjhjbkhXM0FlNURqTnhKdDVlcVlDeUVpN0tSY2hIa2wwakpmOHF4U21HZGZia1lFSHIxQzJmODJPVlo0YzBZVzVSNmVnM1ZSeVJuUUV0YVhBUVNVakEtSGRDZy1tUXoyOU9lcHR5alIyRUxvZmhxaHhjdTdGU1BRT2tSS25tZUVuYVdaWEZOcF9qc2pKYkg1WUxuWkdoemJqU2Ywd2d6clQwYnEzNU1rdzgyb0JDLVBhYyJ9\"}"

  read -n 1 -s -r -p "Press any key to continue..."
  echo 

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
