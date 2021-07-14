#!/bin/bash

# Apache Tomcat download page: https://tomcat.apache.org/download-90.cgi
export TOMCAT_MINOR_VERSION=50

main() {
  install-ssh
  install-java
  install-tomcat
  install-mysql8
  sudo apt install -y git ant curl jq
  echo "Installation complete."
  echo "To configure & test:"
  echo "  1) cd .."
  echo "  2) edit cybrselfserve.properties with your PAS & Conjur config details"
  echo "  3) run: ./ant.sh publish"
  echo "  8) cd ../../servlet-tests"
  echo "  9) run: liveness-test.sh"
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
  cd ../mysql/appgovdb
  ./1-create-db.sh
}

main "$@"
