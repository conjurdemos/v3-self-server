# Self-Server

Tomcat & MySQL servers to support self-service access request workflows.

## Top directory:
 - Vagrantfile - brings up base ubuntu18 vm with 'vagrant up'. vagrant installation script in ./bin/
 - _checkin.sh - git add/commit/push to specified branch

## bin:
 - setup_vagrant.sh - installs/configures vagrant
 - cybrselfserve-install.sh - installation script
 - tomcat-users.xml - creates admin user w/ Cyberark1 password
 - restart_servers.sh - stops/starts mysql and tomcat servers

## build:
 - ant.sh - build script driven from build.xml
 - build.xml - declarative build instructions for Java/Tomcat
 - cybrselfserve.properties - property file w/ config & authn values
 - web.xml - servlet mapping configuration for cybr endpoints

### src:
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
 - PASAccountDetail.java - structure for gson to hold PASAccountDetail info
 - PASAccountDetailList.java - structure for gson to hold PASAccountDetailList info
 - PASAccountList.java - structure for gson to hold PASAccountList info
 - PASAccountServlet.java - POST/DELETE functions to create/delete accounts in safes (not used)
 - PASBasicAuthFilter.java - supports admin/pasword authn to PVWA
 - PASJava.java - Java wrappers for PAS REST APIs
 - PASPlatformProperties.java - structure for gson to hold PASPlatformProperties info
 - PASRemoteMachinesAccess.java - structure for gson to hold PASRemoteMachinesAccess info
 - PASSafeServlet.java - POST/DELETE functions to create/delete Safes in PAS
 - PASSecretManagement.java - structure for gson to hold PASSecretManagement info
 - PASServlet.java - placeholder for basic auth
 - ProvisioningServlet.java - POST/DELETE functions that call other servlets to provision/deprovision access requests
 - old - code that my still be useful maybe?

### dependencies:
 - gson-2.8.5.jar
 - mysql-connector-java-8.0.25.jar
 
## mysql:
 - mysql.config - URL of database (container values for MySQL)
 - exec-to-db-server.sh - interactive mysql cli
 - mysql-pids-cleanup.sh - deletes stale connections
 - mysql-pids-status.sh - list active/stale connections to server
 - appgovdb - create/load/query appgovdb
 - azure - for appgovdb in azure (where Sailpoint VM in SkyTap can read it)
 - docinabox - target DB create/load/query
 - petclinic - target db create/load/query

## policy-edit:
 - conjur-delete-project-and-safe.sh - script to delete Conjur policy artifacts for a project & safe corresponding to an access request
 - delete.yml - delete project/safe policy template
 - grant.yml - access grant policy
 - revoke.yml - access revoke policy
 - 
## servlet-tests:
 - appgovdb-get - gets all access requests by status (approved, unprovisioned, provisioned, revoked, rejected)
 - cybrtest.config - provides env vars for scripts to simulate UI input
 - deprovision-with-servlets - access request revocation
 - governance-get - gets json record used in access review (auditor) ui
 - gui-lifecycle - access request submittal, approval, provisioning & revocation
 - provision-with-servlets - access request provisioning
 - tail-catalina-out.sh - follows tomcat log to monitor servlet execution
 - tomcat-debug-logs.sh - cats various logs for debugging
 - vi-tomcat-out.sh - edit tomcat log
 - _old - old test scripts that can probably be deleted

