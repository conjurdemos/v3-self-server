# Self-Server

Tomcat & MySQL servers to support self-service access request workflows.

## Top directory
 - _checkin.sh - git add/commit/push to specified branch
 - ant.sh - build script driven from build.xml
 - build.xml - declarative build instructions for Java/Tomcat
 - cybrselfserve.properties - property file w/ config & authn values
 - startup.sh - starts Tomcat & MySQL servers after power cycle
 - web.xml - servlet mapping configuration for cybr endpoints

install:
 - cybrselfserve-install.sh
 - tomcat-users.xml

servlet-tests:
 - _old
 - appgovdb-get
 - cybrtest.config
 - deprovision-with-servlets
 - governance-get
 - gui-lifecycle
 - provision-with-servlets
 - tail-catalina-out.sh
 - tomcat-debug-logs.sh
 - vi-tomcat-out.sh

sailpoint:
 - sp-integration-notes

scratch:
 - conjur-delete-project-and-safe.sh
 - delete.yml
 - grant.yml
 - revoke.yml

src:
 - AccessRequestParameters.java - structure for gson to hold access request values
 - AppGovDbServlet.java - GET/POST/PUT/DELETE functions for appgovdb
 - Config.java - reads & stores property file values in variables, and disables cert validation
 - ConjurAccessPolicyServlet.java - grants/revokes safe access role to/from identity
 - ConjurBasePolicyServlet.java - POST/DELETE functions to create/delete project base policies
 - ConjurBasicAuthFilter.java - supports admin/pasword authn to Conjur
 - ConjurIdentityPolicyServlet.java - POST/DELETE functions to create/delete host identities in projects
 - ConjurJava.java - Java wrappers for Conjur REST APIs
 - ConjurSafePolicyServlet.java - POST/DELETE functions to create/delete consumer policies for safes in projects
 - ConjurServlet.java - placeholder for basic auth
 - CybrDriver.java - Java main for testing w/o servlets
 - GovernanceServlet.java - GET function to retrieve project/identity json for provisioned requests
 - JavaREST.java - Java HTTP functions (GET/POST/PATCH/DELETE)
 - KeyValue.java - structure for gson
 - PASAccount.java - structure for gson to hold PASAccount info
 - PASAccountDetail.java - structure for gson to hold PASAccount info
 - PASAccountDetailList.java - structure for gson to hold PASAccount info
 - PASAccountList.java - structure for gson to hold PASAccount info
 - PASAccountServlet.java - POST/DELETE functions to create/delete accounts in safes (not used)
 - PASBasicAuthFilter.java - supports admin/pasword authn to PVWA
 - PASJava.java - Java wrappers for PAS REST APIs
 - PASPlatformProperties.java - structure for gson to hold PASAccount info
 - PASRemoteMachinesAccess.java - structure for gson to hold PASAccount info
 - PASSafeServlet.java - POST/DELETE functions to create/delete Safes in PAS
 - PASSecretManagement.java - structure for gson to hold PASAccount info
 - PASServlet.java - placeholder for basic auth
 - ProvisioningServlet.java - POST/DELETE functions that call other servlets to provision/deprovision access requests
 - old - code that my still be useful maybe?

## dependencies:
gson-2.8.5.jar
mysql-connector-java-8.0.25.jar

## mysql:
 - mysql.config - URL of database (container values for MySQL)
 - exec-to-db-server.sh - interactive mysql cli
 - mysql-pids-cleanup.sh - deletes stale connections
 - mysql-pids-status.sh - list active/stale connections to server
 - appgovdb - create/load/query appgovdb
 - azure - for appgovdb in azure (where Sailpoint VM in SkyTap can read it)
 - docinabox - target DB create/load/query
 - petclinic - target db create/load/query
