#!/bin/bash
source ../mysql.config
cat $1 \
| mysqldump -u root --password=$MYSQL_ROOT_PASSWORD appgovdb
