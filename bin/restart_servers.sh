#!/bin/bash
sudo service mysql stop
sudo service mysql start
/usr/local/tomcat9/bin/shutdown.sh
/usr/local/tomcat9/bin/startup.sh
