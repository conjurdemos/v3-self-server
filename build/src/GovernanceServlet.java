/*
  Help on how to write a servlet that accepts json input payloads:
   https://stackoverflow.com/questions/3831680/httpservletrequest-get-json-post-data

 MySQL best-practice to avoid dangling connections at server:
  - Create connection
  - Create cursor/prepared statement
  - Create Query string
  - Execute the query
  - Commit the query
  - Close cursor/prepared statement
  - Close the connection
*/

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;

import com.google.gson.Gson;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ###########################################
public class GovernanceServlet extends HttpServlet {
  /** Logger */
  private static final Logger logger = Logger.getLogger(GovernanceServlet.class.getName());

  private static Connection dbConn = null;

  // +++++++++++++++++++++++++++++++++++++++++
  // Initialize values in case no one else has
  @Override
  public void init() {
    try {
      InputStream inputStream = getServletContext().getResourceAsStream(Config.propFileName);
      Config.loadConfigValues(inputStream);
    } catch (IOException e) {
      System.out.println("Exception: " + e);
    } 
    PASJava.initConnection(Config.pasIpAddress);
    Config.disableCertValidation();
  }

  // +++++++++++++++++++++++++++++++++++++++++
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {

    String jsonArrayName = "";
    // connect to database
    try {
      GovernanceServlet.dbConn = DriverManager.getConnection(Config.appGovDbUrl,
							   Config.appGovDbUser,
							   Config.appGovDbPassword);
      GovernanceServlet.dbConn.setAutoCommit(false);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error connecting to appgovdb.");
      e.printStackTrace();
    }

    Connection conn =  GovernanceServlet.dbConn;
    String querySql = "";
    PreparedStatement prepStmt = null;
    String returnJson = "";

    /*
    Code in first try block rebuilds the cybraccounts table. It truncates the table to delete
    all rolws then retrieves all the  accounts in a safe for any active (provisioned, not revoked)
    access request. Retrieval is done here to ensure identity access views are up-to-date with the
    contents of safes.
    Currently only database accounts with non-null database properties are supported.
    */
    PASJava.logon(Config.pasAdminUser, Config.pasAdminPassword);
    Gson gson = new Gson();				// parse json output into PASAccountList
    try {
      querySql = "TRUNCATE TABLE cybraccounts";		// delete all rows in cybraccounts
      prepStmt = conn.prepareStatement(querySql);
      prepStmt.executeUpdate();

      querySql = "SELECT sf.id, sf.name"
	      	+ " FROM safes sf, accessrequests ar"
		+ " WHERE ar.provisioned AND NOT ar.revoked "
		+ " AND ar.safe_id = sf.id";
      prepStmt = conn.prepareStatement(querySql);
      ResultSet rsSf = prepStmt.executeQuery();
      while(rsSf.next() ) {				// for each safe
        String safeId = rsSf.getString(1);
        String safeName = rsSf.getString(2);
							// get list of accounts
	String pasAccountJson = PASJava.getAccounts(safeName);
        PASAccountList accList = (PASAccountList) gson.fromJson(pasAccountJson, PASAccountList.class );
        querySql = "INSERT IGNORE INTO cybraccounts"
			+ " (safe_id, name, platform_id, secret_type, username, address, resource_type, resource_name)"
			+ " VALUES"
			+ " (?,?,?,?,?,?,?,?)";
        prepStmt = conn.prepareStatement(querySql);
        for(int i = 0; i < accList.value.length; i++) {	// for each account in safe

          // determine if account is for a database based on account platform properties,
	  // skip if not a database or does not name a database
          String resourceType = "";
          String resourceName = "";
          if(accList.value[i].platformAccountProperties != null) {
            if(accList.value[i].platformAccountProperties.Database != null) {
              resourceType = "database";
	      resourceName = accList.value[i].platformAccountProperties.Database;
	    }
          }
          if(resourceType == "") {
            logger.log(Level.INFO, "Access for account \'" + accList.value[i].name + "\' will not be recorded. Only PAS database accounts with non-empty property values for 'database' are supported.");
	    continue;
	  }

          prepStmt.setString(1, safeId);
          prepStmt.setString(2, accList.value[i].name);
          prepStmt.setString(3, accList.value[i].platformId);
          prepStmt.setString(4, accList.value[i].secretType);
          prepStmt.setString(5, accList.value[i].userName);
          prepStmt.setString(6, accList.value[i].address);
          prepStmt.setString(7, resourceType);
          prepStmt.setString(8, resourceName);
          prepStmt.executeUpdate();
          conn.commit();
          logger.log(Level.INFO, "write account records :"
                                + "\n  query template: " + querySql
                                + "\n  values: "
                                + safeId + ", "
				+ accList.value[i].name + ", "
				+ accList.value[i].platformId + ", "
				+ accList.value[i].secretType + ", "
				+ accList.value[i].userName + ", "
				+ accList.value[i].address + ", "
                                + resourceType + ", "
                                + resourceName);
        } // for each account in safe
      } // for each safe
      prepStmt.close();
    } catch (SQLException e) {
      logger.log(Level.INFO, "Error id identity access query:\n  query template: " + querySql);
      e.printStackTrace();
    }

    // this try block assembles the json response
    try {
      // Get distinct provisioned project IDs
      querySql = "SELECT DISTINCT ar.project_id, pr.name"
		+ " FROM accessrequests ar, projects pr"
		+ " WHERE ar.provisioned AND NOT ar.revoked"
		+ " AND ar.project_id = pr.id";
      prepStmt = conn.prepareStatement(querySql);
      ResultSet rsPr = prepStmt.executeQuery();
      String prJson = "";
      while(rsPr.next() ) {		// for each provisioned project ID
        if(prJson != "") {
          prJson = prJson + ",";
	}
        int prId = rsPr.getInt(1);
        String prName = rsPr.getString(2);
        querySql = "SELECT appid.name, ca.resource_type, ca.resource_name, ca.username, sf.name"
		+ " FROM appidentities appid, accessrequests ar, safes sf, cybraccounts ca"
		+ " WHERE ar.provisioned AND NOT ar.revoked"
		+ " AND ar.project_id = ?"
		+ " AND appid.project_id = ?"
		+ " AND ar.app_id = appid.id"
		+ " AND ar.safe_id = ca.safe_id"
		+ " AND ca.safe_id = sf.id";
        logger.log(Level.INFO, "Provisioned projects query:"
                                + "\n  query template: " + querySql
				+ "\n  projectId: " + Integer.toString(prId));
        prepStmt = conn.prepareStatement(querySql);
        prepStmt.setInt(1, prId);
        prepStmt.setInt(2, prId);
        ResultSet rsId = prepStmt.executeQuery();
        String idJson = "";
        while(rsId.next() ) {		// for app identity in project
          if(idJson != "") {
            idJson = idJson + ",";
	  }
          String idRecord = "{\"appId\": \"" + rsId.getString(1) + "\""
			+ ",\"resourceType\": \"" + rsId.getString(2) + "\""
			+ ",\"resourceName\": \"" + rsId.getString(3) + "\""
			+ ",\"username\": \"" + rsId.getString(4) + "\""
			+ ",\"safeName\": \"" + rsId.getString(5) + "\"}";
	  idJson = idJson + idRecord;
	}
        prJson = prJson + "{\"projectName\": \"" + prName + "\",\n"
			+ "\"identities\": [" + idJson + "]}";
      }
      returnJson = "{\"projects\": [" + prJson + "]}";
      conn.commit();
      prepStmt.close();
    } catch (SQLException e) {
      logger.log(Level.INFO, "Error id identity access query:\n  query template: " + querySql);
      e.printStackTrace();
    }

    // close the database connection
    try {
      GovernanceServlet.dbConn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    response.getOutputStream().println(returnJson);

  } // doGet

} // GovernanceServlet
