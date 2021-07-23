/*
 * Defines REST endpoints to:
 * - get named user of userType EPVUser from PAS
 */

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStream;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

// ###########################################
// GET = get named user, verify as userType EPVUser
public class PASUserServlet extends HttpServlet {
    /** Logger */
    private static final Logger logger = Logger.getLogger(PASUserServlet.class.getName());

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
    Config.disableCertValidation();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
    String userName = request.getParameter("userName");

    String pasToken = PASJava.logon(Config.pasAdminUser, Config.pasAdminPassword);

    String userListJson = PASJava.getUsers("filter=userName&search="+userName);
    logger.log(Level.INFO, "User query returned: " + userListJson);
    String userResponse = "{\"users\": [] }";
    if (userListJson != null) {
      Gson gson = new Gson();
      PASUserList userList = (PASUserList) gson.fromJson(userListJson, PASUserList.class );
      if ((userList.Total == 1) && (userList.Users[0].userType.equals("EPVUser"))) {
        userResponse = "{\"users\": ["
      				+"{ \"userName\": \"" + userList.Users[0].username + "\","
				+ "\"userType\": \"" + userList.Users[0].userType + "\"}"
				+ "] }";
      }
    }
    response.getOutputStream().println(userResponse);

  } // doGet

} // PASUserServlet
