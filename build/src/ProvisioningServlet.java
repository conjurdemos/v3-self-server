/*
 * Defines REST endpoints to:
 *  - Provision an access request, includes creating empty safe & Conjur policies
 *  - Deprovision (revoke) an access request, only includes revoking access to safe

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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Random;
import java.sql.*;

// ###########################################
public class ProvisioningServlet extends HttpServlet {
  /** Logger */
  private static final Logger logger = Logger.getLogger(ProvisioningServlet.class.getName());
  private static Connection dbConn = null;

  // +++++++++++++++++++++++++++++++++++++++++
  // Initialize config object from properties file
  @Override
  public void init() {
    try {
      InputStream inputStream = getServletContext().getResourceAsStream(Config.propFileName);
      Config.loadConfigValues(inputStream);
    } catch (IOException e) {
      System.out.println("Exception: " + e);
    }
    PASJava.initConnection(Config.pasIpAddress);
    ConjurJava.initConnection(Config.conjurUrl,Config.conjurAccount);
    Config.disableCertValidation();
  }

  // +++++++++++++++++++++++++++++++++++++++++
  // Provision an access request by calling other servlets, including:
  //  - adding an LOB user to the requested safe
  //  - loading the Conjur Synchronizer policy for safe
  //  - creating the Conjur safe consumer role in the project policy
  //  - creating the Conjur host identity in the project policy
  //  - granting the project's safe consumer role to host.
  // 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)  
        throws ServletException, IOException {  
    String accReqId = request.getParameter("accReqId");

    String pasToken = PASJava.logon(Config.pasAdminUser, Config.pasAdminPassword);
    String conjurApiKey = ConjurJava.authnLogin(Config.conjurAdminUser, Config.conjurAdminPassword);
    String conjurToken = ConjurJava.authenticate(Config.conjurAdminUser, conjurApiKey);
    if ( Objects.isNull(pasToken) || Objects.isNull(conjurToken) ) {
      throw new ServletException("Error authenticating, pasToken: "+pasToken+", conjurToken: "+conjurToken);
    }
    try {
      ProvisioningServlet.dbConn = DriverManager.getConnection(Config.appGovDbUrl,
								Config.appGovDbUser,
								Config.appGovDbPassword);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    				// assemble access request parameter object from db
    AccessRequestParameters arParms = getAccessRequestParameters(accReqId);
    arParms.print();

    String safeAddMemberResponse = addSafeMember(	arParms.pasSafeName,
							arParms.pasLobName);
    String safeSyncPolicyResponse = createSafeSyncPolicy(
							arParms.pasVaultName,
							arParms.pasLobName,
							arParms.pasSafeName);
    String projectSafePolicyResponse = createProjectSafePolicy(
							arParms.projectName,
							arParms.pasVaultName,
							arParms.pasLobName,
							arParms.pasSafeName);
    String projectIdentityPolicyResponse = createProjectIdentityPolicy(
							arParms.projectName,
							arParms.appIdName);
    String projectAccessPolicyResponse = createProjectAccessPolicy(
							arParms.projectName,
							arParms.appIdName,
							arParms.pasSafeName);

    // mark accessrequest provisioned
    String requestUrl = Config.selfServeBaseUrl+ "/appgovdb?accReqId="+ accReqId
					+ "&status=provisioned";
    String markedProvisionedResponse = JavaREST.httpPut(requestUrl, "", "");

    logger.log(Level.INFO, "Provision access request: "
		+ "\n  safeAddMemberResponse:" + safeAddMemberResponse + ","
		+ "\n  safeSyncPolicyResponse:" + safeSyncPolicyResponse + ","
		+ "\n  projectSafePolicyResponse:" + projectSafePolicyResponse + ","
		+ "\n  projectIdentityPolicyResponse: " + projectIdentityPolicyResponse 
		+ "\n  projectAccessPolicyResponse: " + projectAccessPolicyResponse
		+ "\n  markedProvisionedResponse: " + markedProvisionedResponse);

    response.getOutputStream().println("{"
	+ "\"safeAddMemberResponse\": " + safeAddMemberResponse + ", \""
	+ "\"safeSyncPolicyResponse\": " + safeSyncPolicyResponse + ", \""
	+ "\nprojectSafePolicyResponse\": \"" + projectSafePolicyResponse + ", \""
	+ "\nprojectIdentityPolicyResponse\": \"" + projectIdentityPolicyResponse + ", \""
	+ "\nprojectAccessPolicyResponse\": \"" + projectAccessPolicyResponse + ", \""
	+ "\nmarkedProvisionedResponse\": \"" + markedProvisionedResponse
					+ "\"}");

  } // doPost
  
  // +++++++++++++++++++++++++++++++++++
  // Assemble new AccessRequestParameters object from database
  private static AccessRequestParameters getAccessRequestParameters(String accReqId) {
    Connection conn = ProvisioningServlet.dbConn;
    String requestUrl = "";
    String safeResponse = "";
    AccessRequestParameters arParms = new AccessRequestParameters();
    try {
      String querySql = "SELECT pr.name, ar.requestor, ar.approved, ar.environment, sf.vault_name, sf.name, sf.cpm_name, ar.lob_name, app.name, app.authn_method"
			+ " FROM accessrequests ar, safes sf, projects pr, appidentities app"
			+ " WHERE ar.id = ?"
			+ " AND ar.safe_id = sf.id"
			+ " AND ar.project_id = pr.id"
			+ " AND ar.app_id = app.id";
      System.out.println("executing query: " + querySql + " with accReqId: " + accReqId);
      PreparedStatement prepStmt = conn.prepareStatement(querySql);
      prepStmt.setString(1, accReqId);
      ResultSet rs = prepStmt.executeQuery();
      while(rs.next()) { 		// unique access request id guarantees only one row returned
    	arParms.projectName = rs.getString("pr.name");
    	arParms.requestor = rs.getString("ar.requestor");
    	arParms.approved = rs.getInt("ar.approved");
    	arParms.environment = rs.getString("ar.environment");
    	arParms.pasVaultName = rs.getString("sf.vault_name");
    	arParms.pasSafeName = rs.getString("sf.name");
    	arParms.pasCpmName = rs.getString("sf.cpm_name");
    	arParms.pasLobName = rs.getString("ar.lob_name");
    	arParms.appIdName = rs.getString("app.name");
    	arParms.appAuthnMethod = rs.getString("app.authn_method");
      }
      prepStmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    arParms.print();
    return arParms;
  }

  // +++++++++++++++++++++++++++++++++++
  // Add LOB user as member of safe for syncing to Conjur
  private static String addSafeMember(String safeName, String lobName) {
    String requestUrl = Config.selfServeBaseUrl + "/safe/member"
					+ "?safeName=" + safeName
   		                        + "&lobName=" + lobName;
    logger.log(Level.INFO, "Add safe member: " + requestUrl);
    String safeAddMemberResponse = JavaREST.httpPost(requestUrl, "", "");
    return safeAddMemberResponse;
  }

  // +++++++++++++++++++++++++++++++++++
  // Create Conjur safe synchronizer policy for project
  private static String createSafeSyncPolicy(	String vaultName,
						String lobName,
						String safeName) {
    String requestUrl = Config.selfServeBaseUrl + "/safe/syncpolicy"
					+ "?vaultName=" + vaultName
   		                        + "&lobName=" + lobName
   		                        + "&safeName=" + safeName;
    logger.log(Level.INFO, "Create safe synch policy: " + requestUrl);
    return JavaREST.httpPost(requestUrl, "", "");
  }

  // +++++++++++++++++++++++++++++++++++
  // Create Conjur safe consumer policy for project
  private static String createProjectSafePolicy(String projectName,
						String vaultName,
						String lobName,
						String safeName) {
    String requestUrl = Config.selfServeBaseUrl + "/project/safepolicy"
					+ "?projectName=" + projectName
					+ "&vaultName=" + vaultName
   		                        + "&lobName=" + lobName
   		                        + "&safeName=" + safeName;
    logger.log(Level.INFO, "Create safe synch policy: " + requestUrl);
    return JavaREST.httpPost(requestUrl, "", "");
  }

  // +++++++++++++++++++++++++++++++++++
  // Create Conjur identity policy for project
  private static String createProjectIdentityPolicy(
		  				String projectName,
						String idName) {
    String requestUrl = Config.selfServeBaseUrl + "/project/identitypolicy"
   		                               + "?projectName=" + projectName
   		                               + "&identityName=" + idName;
    logger.log(Level.INFO, "Create project identity policy: " + requestUrl);
    return JavaREST.httpPost(requestUrl, "", "");
  }

  // +++++++++++++++++++++++++++++++++++
  // Grant project safe/consumers group role to identity
  private static String createProjectAccessPolicy(
		  				String projectName,
						String appIdName,
						String safeName) {
    String requestUrl = Config.selfServeBaseUrl + "/project/accesspolicy"
   		                             + "?projectName=" + projectName
   		                             + "&identityName=" + appIdName
   		                             + "&groupRoleName=" + safeName + "/consumers";
    logger.log(Level.INFO, "Grant project access policy: " + requestUrl);
    return JavaREST.httpPost(requestUrl, "", "");
  }

  // +++++++++++++++++++++++++++++++++++++++++
  // Revokes access grant and deletes safe consumers group for a project, thereby 
  // removing a project's access to accounts in the safe, and preventing the
  // project admin from re-granting access.
  // This is not a true inverse of provisioning as it does not delete identities, base policies,
  // or safes.  During development, it became clear that knowing exactly what to 
  // delete is a complex problem.
  // Those functions are preserved should they prove useful in the future.
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
    String accReqId = request.getParameter("accReqId");

    String pasToken = PASJava.logon(Config.pasAdminUser, Config.pasAdminPassword);
    String conjurApiKey = ConjurJava.authnLogin(Config.conjurAdminUser, Config.conjurAdminPassword);
    String conjurToken = ConjurJava.authenticate(Config.conjurAdminUser, conjurApiKey);
    if ( Objects.isNull(pasToken) || Objects.isNull(conjurToken) ) {
      throw new ServletException("Error authenticating, pasToken: "+pasToken+", conjurToken: "+conjurToken);
    }

    try {
      ProvisioningServlet.dbConn = DriverManager.getConnection(Config.appGovDbUrl,
								Config.appGovDbUser,
								Config.appGovDbPassword);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    String accessPolicyResponse = revokeAccessPolicy(accReqId);
    String safePolicyResponse = deleteSafePolicy(accReqId);
//    String identityPolicyResponse = deleteIdentityPolicy(accReqId);
//    String basePolicyResponse = deleteBasePolicy(accReqId);
//    String accountResponse = deleteAccounts(accReqId);
//    String safeResponse = deleteSafes(accReqId);

    // mark access request revoked

    String requestUrl = Config.selfServeBaseUrl+ "/appgovdb?accReqId="+ accReqId
						+ "&status=revoked";
    String markedRevokedResponse = JavaREST.httpPut(requestUrl, "", "");

    response.getOutputStream().println("{"
					+ accessPolicyResponse + ", "
					+ markedRevokedResponse + "}");

//					+ basePolicyResponse + ","
//					+ safePolicyResponse + ","
//					+ identityPolicyResponse + ","
//					+ safeResponse + ","
//					+ accountResponse + ","

  } //doDelete

  // ++++++++++++++++++++++++++++++++
  // Revoke safe/consumers group role for identity
  private static String revokeAccessPolicy(String accReqId) {
    Connection conn = ProvisioningServlet.dbConn;
    String requestUrl = "";
    String accessPolicyResponse = "";
    try {
      String querySql = "SELECT pr.name, appid.name, sf.name "
		+ "FROM projects pr, appidentities appid, accessrequests ar, safes sf "
		+ "WHERE ar.id = ? AND ar.app_id = appid.id AND ar.project_id = pr.id AND ar.safe_id = sf.id";
      PreparedStatement prepStmt = conn.prepareStatement(querySql);
      prepStmt.setString(1, accReqId);
      ResultSet rs = prepStmt.executeQuery();
      while(rs.next()) {
        String projectName = rs.getString("pr.name");
        String idName = rs.getString("appid.name");
        String safeName = rs.getString("sf.name");
        requestUrl = Config.selfServeBaseUrl + "/project/accesspolicy"
   		                             + "?projectName=" + projectName
   		                             + "&identityName=" + idName
   		                             + "&groupRoleName=" + safeName + "/consumers";
        logger.log(Level.INFO, "Delete access policy: " + requestUrl);
        accessPolicyResponse = accessPolicyResponse + Integer.toString(JavaREST.httpDelete(requestUrl, ""));
      }
      prepStmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return accessPolicyResponse;
  } // revokeAccessPolicy

  // +++++++++++++++++++++++++++++++++
  // Delete Conjur identity(s) for project
  private static String deleteIdentityPolicy(String accReqId) {
    Connection conn = ProvisioningServlet.dbConn;
    String requestUrl = "";
    String identityPolicyResponse = "";
    try {
      String querySql =  "SELECT pr.name, appid.name "
		+ "FROM projects pr, appidentities appid, accessrequests ar "
		+ "WHERE ar.id = ? AND ar.project_id = pr.id AND ar.app_id = appid.id";
      PreparedStatement prepStmt = conn.prepareStatement(querySql);
      prepStmt.setString(1, accReqId);
      ResultSet rs = prepStmt.executeQuery();
      while(rs.next()) {
        String projectName = rs.getString("pr.name");
        String idName = rs.getString("appid.name");
        requestUrl = Config.selfServeBaseUrl + "/project/identitypolicy"
   		                               + "?projectName=" + projectName
   		                               + "&identityName=" + idName;
        logger.log(Level.INFO, "Delete identity policy: " + requestUrl);
        identityPolicyResponse = identityPolicyResponse + Integer.toString(JavaREST.httpDelete(requestUrl, ""));
      }
      prepStmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return identityPolicyResponse;
  } // deleteIdentityPolicy

  // +++++++++++++++++++++++++++++++++
  // Delete Conjur safe policy(s) for project
  private static String deleteSafePolicy(String accReqId) {
    Connection conn = ProvisioningServlet.dbConn;
    String requestUrl = "";
    String safePolicyResponse = "";
    try {
      String querySql = "SELECT pr.name, sf.name, sf.vault_name, ar.lob_name "
		+ " FROM accessrequests ar, projects pr, safes sf "
		+ " WHERE ar.id = ? AND ar.project_id = pr.id AND ar.safe_id = sf.id";
      PreparedStatement prepStmt = conn.prepareStatement(querySql);
      prepStmt.setString(1, accReqId);
      ResultSet rs = prepStmt.executeQuery();
      while(rs.next()) {
        String projectName = rs.getString("pr.name");
        String safeName = rs.getString("sf.name");
        String vaultName = rs.getString("sf.vault_name");
        String lobName = rs.getString("ar.lob_name");
        requestUrl = Config.selfServeBaseUrl + "/project/safepolicy"
     		                               + "?projectName=" + projectName
   		                               + "&vaultName=" + vaultName
   		                               + "&lobName=" + lobName
   		                               + "&safeName=" + safeName;
        logger.log(Level.INFO, "Add safe project policy: " + requestUrl);
        safePolicyResponse = Integer.toString(JavaREST.httpDelete(requestUrl, ""));
      }
      prepStmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return safePolicyResponse;
  } // deleteSafePolicy

  // ++++++++++++++++++++++++++++++++++
  // Delete Conjur base policy for project
  private static String deleteBasePolicy(String accReqId) {
    Connection conn = ProvisioningServlet.dbConn;
    String requestUrl = "";
    String basePolicyResponse = "";
    try {
      String querySql = "SELECT pr.name, pr.admin_user"
		+ " FROM projects pr, accessrequests ar"
		+ " WHERE ar.id = ? AND ar.project_id = pr.id";
      PreparedStatement prepStmt = conn.prepareStatement(querySql);
      prepStmt.setString(1, accReqId);
      ResultSet rs = prepStmt.executeQuery();
      while(rs.next()) {
        String projectName = rs.getString("pr.name");
        String adminName = rs.getString("pr.admin_user");

        requestUrl = Config.selfServeBaseUrl + "/project/basepolicy"
   		                               + "?projectName=" + projectName
   		                               + "&adminName=" + adminName;
        logger.log(Level.INFO, "Add base project policy: " + requestUrl);
        basePolicyResponse = Integer.toString(JavaREST.httpDelete(requestUrl, ""));
      }
      prepStmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return basePolicyResponse;
  } // deleteBasePolicy

  // +++++++++++++++++++++++++++++++++++
  // Delete accounts from project safe(s)
  private static String deleteAccounts(String accReqId) {
    Connection conn = ProvisioningServlet.dbConn;
    String requestUrl = "";
    String accountResponse = "";
    try {
      String querySql = "SELECT sf.name, ca.name, ca.platform_id, ca.address, ca.username,c a.secret_type"
		+ " FROM accessrequests ar, projects pr, safes sf, cybraccounts ca"
		+ " WHERE ar.id = ? AND ar.project_id = pr.id AND ar.safe_id = sf.id and ca.safe_id = sf.id";
      PreparedStatement prepStmt = conn.prepareStatement(querySql);
      prepStmt.setString(1, accReqId);
      ResultSet rs = prepStmt.executeQuery();
      while (rs.next()) {
        String safeName = rs.getString("sf.name");
        String accountName = rs.getString("ca.name");
        String platformId = rs.getString("ca.platform_id");
        String accountAddress = rs.getString("ca.address");
        String accountUsername = rs.getString("ca.username");
        String accountSecretType = rs.getString("ca.secret_type");
        requestUrl = Config.selfServeBaseUrl + "/pas/accounts"
						+ "?safeName=" + safeName
						+ "&accountName=" + accountName
                                		+ "&platformId=" + platformId
                                		+ "&address=" + accountAddress
                                		+ "&userName=" + accountUsername
                                		+ "&secretType=" + accountSecretType
                                		+ "&secretValue=" + "RAndo498578x";
        logger.log(Level.INFO, "Add account: " + requestUrl);
	accountResponse = accountResponse + Integer.toString(JavaREST.httpDelete(requestUrl, "")) + ",";
	prepStmt.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return accountResponse;
  } // deleteAccounts

  // ++++++++++++++++++++++++++++++
  // Delete safe and Conjur synch policy
  private static String deleteSafe(String accReqId) {
    Connection conn = ProvisioningServlet.dbConn;
    String requestUrl = "";
    String safeResponse = "";
    try {
      String querySql = "SELECT sf.name, sf.cpm_name, sf.vault_name, ar.lob_name"
			+ " FROM accessrequests ar, safes sf WHERE ar.id = ? AND ar.safe_id = sf.id";
      PreparedStatement prepStmt = conn.prepareStatement(querySql);
      prepStmt.setString(1, accReqId);
      ResultSet rs = prepStmt.executeQuery();
      while(rs.next()) { 		// unique access request id guarantees only one row returned
        String safeName = rs.getString("sf.name");
        String cpmName = rs.getString("sf.cpm_name");
        String vaultName = rs.getString("sf.vault_name");
        String lobName = rs.getString("ar.lob_name");

        requestUrl = Config.selfServeBaseUrl + "/pas/safes"
  						+ "?safeName=" + safeName
                               			+ "&cpmName=" + cpmName
                               			+ "&lobName=" + lobName
                               			+ "&vaultName=" + vaultName;
        logger.log(Level.INFO, "Add safe: " + requestUrl);
        safeResponse = Integer.toString(JavaREST.httpDelete(requestUrl, ""));
      }
      prepStmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return safeResponse;
  } // deleteSafes

  // +++++++++++++++++++++++++++++++++++
  // DEPRECATED - preserved for reference
  // Automated account creation is not supported
  // Add specified accounts to the safe 
  private static String addAccounts(String accReqId) {
    Connection conn = ProvisioningServlet.dbConn;
    String requestUrl = "";
    String accountResponse = "";
    String querySql = "SELECT sf.name, ca.name, ca.platform_id, ca.address, ca.username, ca.secret_type"
		+ " FROM accessrequests ar, cybraccounts ca, projects pr, safes sf "
		+ " WHERE ar.id = ? AND ar.project_id = pr.id AND ar.safe_id = sf.id AND ca.safe_id = sf.id";
    try {
      PreparedStatement prepStmt = conn.prepareStatement(querySql);
      prepStmt.setString(1, accReqId);
      ResultSet rs = prepStmt.executeQuery();
      while (rs.next()) {
        String safeName = rs.getString("sf.name");
        String accountName = rs.getString("ca.name");
        String platformId = rs.getString("ca.platform_id");
        String accountAddress = rs.getString("ca.address");
        String accountUsername = rs.getString("ca.username");
        String accountSecretType = rs.getString("ca.secret_type");
        requestUrl = Config.selfServeBaseUrl + "/pas/accounts"
				+ "?safeName=" + safeName
                                + "&accountName=" + accountName
                                + "&platformId=" + platformId
                                + "&address=" + accountAddress
                                + "&userName=" + accountUsername
                                + "&secretType=" + accountSecretType
                                + "&secretValue=" + "RAndo498578x";
        logger.log(Level.INFO, "Add account: " + requestUrl);
	accountResponse = accountResponse + JavaREST.httpPost(requestUrl, "", "") + ",";
	prepStmt.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return accountResponse;
  } // addAccounts

} // ProvisioningServlet
