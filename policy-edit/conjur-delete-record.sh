#!/bin/bash
if [[ "$#" != 1 ]]; then
  echo "Usage: $0 <delete_policy_filename>"
  exit -1
fi
cat $1	\
  | docker exec -i conjur-cli conjur policy load --delete root -
