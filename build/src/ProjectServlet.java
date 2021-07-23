/*
  Implements:
  - doGet(projectName) - returns info on all projects if none specfied
  - doPost() <json body> - reads json for new project and inserts values into DB
*/

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
public class ProjectServlet extends HttpServlet {
  /** Logger */
  private static final Logger logger = Logger.getLogger(ProjectServlet.class.getName());

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
  }

  // +++++++++++++++++++++++++++++++++++++++++
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
    String projectName = request.getParameter("projectName");

    String whereFilter = "";	// default is to get all projects unless one specified
    Boolean getAll = true;
    if(projectName!=null && !projectName.isEmpty()){
      getAll = false;
      whereFilter = " WHERE name = ?";
    }

    String jsonArrayName = "";
    // connect to database
    try {
      ProjectServlet.dbConn = DriverManager.getConnection(Config.appGovDbUrl,
							   Config.appGovDbUser,
							   Config.appGovDbPassword);
      ProjectServlet.dbConn.setAutoCommit(false);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error connecting to appgovdb.");
      e.printStackTrace();
    }

    Connection conn =  ProjectServlet.dbConn;
    String querySql = "";
    PreparedStatement prepStmt = null;
    String returnJson = "";

    // this try block assembles the json response
    try {
      // Get distinct provisioned project IDs
      querySql = "SELECT id, name, admin_user, billing_code"
		+ " FROM projects"
		+ whereFilter;
      prepStmt = conn.prepareStatement(querySql);
      if (!getAll) {
        prepStmt.setString(1, projectName);
      }
      ResultSet rsPr = prepStmt.executeQuery();
      String prJson = "";
      while(rsPr.next() ) {		// for each project
        if(prJson != "") {
          prJson = prJson + ",";
	}
        int prId = rsPr.getInt(1);
        String prName = rsPr.getString(2);
        String prAdminName = rsPr.getString(3);
        String prBillingCode = rsPr.getString(4);
        logger.log(Level.INFO, "Project query:"
                                + "\n  query template: " + querySql
				+ "\n  projectId: " + Integer.toString(prId)
				+ "\n  projectName: " + prName);
        prJson = prJson + "{"
			+ "\"id\": \"" + Integer.toString(prId) + "\",\n"
			+ "\"name\": \"" + prName + "\",\n"
			+ "\"adminName\": \"" + prAdminName + "\",\n"
			+ "\"billingCode\": \"" + prBillingCode + "\""
			+ "}";
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
      ProjectServlet.dbConn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    response.getOutputStream().println(returnJson);

  } // doGet

  // +++++++++++++++++++++++++++++++++++++++++
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    String jb = "";
    String line = null;
    try {
      BufferedReader reader = request.getReader();
      while ((line = reader.readLine()) != null)
        jb = jb + line;
    } catch (Exception e) {
      logger.log(Level.INFO, "Error parsing POST data payload: " + jb);
    }

    // parse account json output into ProjectParameters structure
    Gson gson = new Gson();
    ProjectParameters prParms = (ProjectParameters ) gson.fromJson(jb, ProjectParameters.class );

    // connect to database
    try {
      ProjectServlet.dbConn = DriverManager.getConnection(Config.appGovDbUrl,
                                                           Config.appGovDbUser,
                                                           Config.appGovDbPassword);
      ProjectServlet.dbConn.setAutoCommit(false);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    Connection conn =  ProjectServlet.dbConn;
    String querySql = "";
    PreparedStatement prepStmt = null;

    // Write project variables to projects table 
    try {
      querySql = "INSERT INTO projects(name,admin_user,billing_code) VALUES(?,?,?)";
      prepStmt = conn.prepareStatement(querySql);
      prepStmt.setString(1, prParms.projectName);
      prepStmt.setString(2, prParms.adminName);
      prepStmt.setString(3, prParms.billingCode);
      prepStmt.executeUpdate();
      conn.commit();
      prepStmt.close();
      logger.log(Level.INFO, "write project record:"
                                + "\n  query template: " + querySql
                                + "\n  values: " + prParms.projectName + ", " + prParms.adminName + ", " + prParms.billingCode);
    } catch (SQLException e) {
      logger.log(Level.INFO, "Error adding project:"
                                + "\n  query template: " + querySql
                                + "\n  values: " + prParms.projectName + ", " + prParms.adminName + ", " + prParms.billingCode);
      e.printStackTrace();
    }

  } // doPost
    
} // ProjectServlet
