#!/bin/bash
source ../mysql.config
if [[ $# != 1 ]]; then
  echo "Usage: $0 <sql-script-filename>"
  exit -1
fi
cat $1 \
| mysql -h $MYSQL_HOSTNAME -u root --password=$MYSQL_ROOT_PASSWORD appgovdb