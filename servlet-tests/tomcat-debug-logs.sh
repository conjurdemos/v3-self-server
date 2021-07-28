#!/bin/bash
source ./cybrtest.config

export ACCESS_LOG_LINES=10
export TOMCAT_DATED_LOG_LINES=20
export TOMCAT_SERVER_LOG_LINES=120

DATESTAMP=$(date +%Y-%m-%d)
echo
echo "LOCALHOST_ACCESS_LOG.$DATESTAMP.LOG =================================="
tail -$ACCESS_LOG_LINES $CATALINA_HOME/logs/localhost_access_log.${DATESTAMP}.txt
echo
echo "CATALINA.$DATESTAMP.LOG =================================="
tail -$TOMCAT_DATED_LOG_LINES $CATALINA_HOME/logs/catalina.$DATESTAMP.log
echo
echo "CATALINA.OUT =================================="
tail -$TOMCAT_SERVER_LOG_LINES $CATALINA_HOME/logs/catalina.out
echo

exit

echo "HOST_MANAGER.$DATESTAMP.LOG =================================="
tail -10 $CATALINA_HOME/logs/host-manager.${DATESTAMP}.log
echo
echo "MANAGER.$DATESTAMP.LOG =================================="
tail -10 $CATALINA_HOME/logs/manager.${DATESTAMP}.log
echo
echo "LOCALHOST.$DATESTAMP.LOG =================================="
tail -10 $CATALINA_HOME/logs/localhost.$DATESTAMP.log

