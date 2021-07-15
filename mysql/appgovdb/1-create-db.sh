#!/bin/bash
source ../mysql.config
cat create_empty_appgovdb.sql \
| $DOCKERI mysql -h $MYSQL_HOSTNAME -u root --password=$MYSQL_ROOT_PASSWORD
