#!/bin/bash

# Apache Tomcat download page: https://tomcat.apache.org/download-90.cgi
export TOMCAT_MINOR_VERSION=50

main() {
  install-mysql8	# run this first cuz it requires user input
  init-appgovdb
  install-ssh
  install-java
  install-tomcat
  sudo apt install -y git ant curl jq
  echo "Installation complete."
  echo
  echo "To configure & test:"
  echo "  1) cd ../build"
  echo "  2) edit cybrselfserve.properties with your PAS & Conjur config details"
  echo "  3) run: ./ant.sh publish"
  echo "  4) exit/vagrant ssh, or logoff/logon to set env vars"
  echo "  5) cd ../servlet-tests"
  echo "  6) run: liveness-test.sh"
  echo
  echo "If that looks good, install the UI and you should be ready to demo."
  echo "If not, run 'tomcat_debug_logs.sh' to see what may be the problem."
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
  cp ../build/dependencies/mysql-connector-java-8.0.25.jar /usr/local/tomcat9/lib

  # start tomcat
  cd /usr/local/tomcat9/bin
  chmod +x ./*.sh
  ./startup.sh
}

#############################
install-mysql8() {
  MYSQL_DEB_PKG=mysql-apt-config_0.8.17-1_all.deb
  wget -c https://dev.mysql.com/get/$MYSQL_DEB_PKG
  sudo dpkg -i $MYSQL_DEB_PKG
  rm $MYSQL_DEB_PKG
  sudo apt-get update -y
  sudo apt-get install -y mysql-server
  sudo mysql_secure_installation
}

#############################
init-appgovdb() {
  echo "Initializing appgovdb..."
  pushd ../mysql/appgovdb
  ./1-create-db.sh
  ./2-load-db.sh load_appgovdb.sql
  popd
}

main "$@"
