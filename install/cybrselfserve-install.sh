#!/bin/bash

# Apache Tomcat download page: https://tomcat.apache.org/download-90.cgi
export TOMCAT_MINOR_VERSION=50

main() {
  install-ssh
  install-java
  install-tomcat
  install-mysql8
  sudo apt install -y git ant curl jq python3-tk
  echo "Installation complete."
  echo "To configure & test:"
  echo "  1) cd ~/v3-self-server"
  echo "  2) edit cybrselfserve.properties with your PAS & Conjur config details"
  echo "  3) run: ./ant.sh publish"
  echo "  4) cd mysql/appgovdb"
  echo "  5) run: ./1-create-db.sh"
  echo "  6) run: ./2-db-load.sh db_load_appgovdb.sql"
  echo "  7) run: ./3-db-query.sh dump_all.sql"
  echo "  8) cd ../../servlet-tests"
  echo "  9) run: gui-lifecycle"
}

#############################
install-ssh() {
  sudo apt update -y
  sudo apt install -y openssh-server
  sudo ufw allow ssh
}

#############################
install-java() {
  sudo apt update -y
  sudo apt install -y default-jdk
  JAVA_DIR=$(ls -ld /usr/lib/jvm/java*jdk* | grep -v ^l | awk '{print $9}')
  export JAVA_HOME=$JAVA_DIR 
  export JRE_HOME=$JAVA_DIR 
  echo "export JAVA_HOME="$JAVA_HOME"" >> ~/.bashrc
  echo "export JRE_HOME="$JRE_HOME"" >> ~/.bashrc
}

#############################
install-tomcat() {
  sudo groupadd tomcat
  sudo useradd -s /bin/false -g tomcat -d /opt/tomcat tomcat
  wget http://www-us.apache.org/dist/tomcat/tomcat-9/v9.0.$TOMCAT_MINOR_VERSION/bin/apache-tomcat-9.0.$TOMCAT_MINOR_VERSION.tar.gz
  tar xzf apache-tomcat-9.0.$TOMCAT_MINOR_VERSION.tar.gz
  rm apache-tomcat-9.0.$TOMCAT_MINOR_VERSION.tar.gz
  sudo mv apache-tomcat-9.0.$TOMCAT_MINOR_VERSION /usr/local/tomcat9
  echo "export CATALINA_HOME="/usr/local/tomcat9"" >> ~/.bashrc
  source ~/.bashrc

  # copy tomcat-users.xml file w/ admin rights for UI (see end of file)
  cp ./tomcat-users.xml /usr/local/tomcat9/conf/tomcat-users.xml

  # copy mysql jdbc connector to tomcat lib before starting (saves a restart)
  cp ../dependencies/mysql-connector-java-8.0.25.jar /usr/local/tomcat9/lib

  # start tomcat
  cd /usr/local/tomcat9/bin
  chmod +x ./*.sh
  ./startup.sh
}

#############################
install-mysql5() {
  sudo apt update -y
  sudo apt install -y mysql-server
  sudo service mysql stop
  while [[ "$(ps -aux | grep mysql | grep -v grep)" != "" ]]; do
    sleep 3
  done
  sudo mkdir -p /var/run/mysqld; sudo chown mysql:mysql /var/run/mysqld
  sudo mysqld_safe --skip-grant-tables &
  echo
  echo
  echo "waiting 10 seconds for db to initialize..."
  sleep 10
  ######## MYSQL 5.x
  sudo mysql --user=root \
    -e "UPDATE mysql.user SET authentication_string=PASSWORD('Cyberark1') WHERE user='root'; UPDATE mysql.user SET plugin='mysql_native_password' WHERE user='root'; FLUSH PRIVILEGES;"
  for i in $(ps -aux | grep mysql | grep -v grep | awk '{print $2}'); do
    sudo kill -9 "$i"
  done
  sudo service mysql start
}

#############################
install-mysql8() {
  wget -c https://dev.mysql.com/get/mysql-apt-config_0.8.17-1_all.deb
  sudo dpkg -i mysql-apt-config_0.8.17-1_all.deb
  sudo apt-get update -y
  sudo apt-get install -y mysql-server
  sudo mysql_secure_installation
  sudo service mysql stop
  while [[ "$(ps -aux | grep mysql | grep -v grep)" != "" ]]; do
    sleep 3
  done
  sudo mkdir -p /var/run/mysqld; sudo chown mysql:mysql /var/run/mysqld
  sudo mysqld_safe --skip-grant-tables &
  echo
  echo
  echo "waiting 10 seconds for db to initialize..."
  sleep 10
  sudo mysql --user=root \
    -e "UPDATE mysql.user SET authentication_string=null WHERE user='root'; FLUSH PRIVILEGES; ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Cyberark1'; FLUSH PRIVILEGES;"
  for i in $(ps -aux | grep mysql | grep -v grep | awk '{print $2}'); do
    sudo kill -9 "$i"
  done
  sudo service mysql start
}

main "$@"
