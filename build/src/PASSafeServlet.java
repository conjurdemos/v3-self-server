/*
 * Defines REST endpoints to:
 * - list safes in EPV
 * - create safe in EPV
 * - delete safe in EPV
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
// GET = lookup safe(s) by name
// POST = create safe
// DELETE = delete safe 

public class PASSafeServlet extends HttpServlet {
    /** Logger */
    private static final Logger logger = Logger.getLogger(PASSafeServlet.class.getName());


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
    String safeName = request.getParameter("safeName");

    String pasToken = PASJava.logon(Config.pasAdminUser, Config.pasAdminPassword);

    String safeListJson = PASJava.listSafes("filter=safeName&search="+safeName);
    logger.log(Level.INFO, "Safe query returned: " + safeListJson);
    String safeResponse = "{\"safes\": [] }";
    if (safeListJson != null) {
      Gson gson = new Gson();
      PASSafeList safeList = (PASSafeList) gson.fromJson(safeListJson, PASSafeList.class );
      if (safeList.count == 1) {
        safeResponse = "{\"safes\": ["
                                +"{ \"safeName\": \"" + safeList.value[0].safeName + "\","
                                + "\"safeDescription\": \"" + safeList.value[0].description+ "\"}"
                                + "] }";
      }
    }
    response.getOutputStream().println(safeResponse);
  } // list safes

  // +++++++++++++++++++++++++++++++++++++++++
  // add safe
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
    String safeName = request.getParameter("safeName");
    String cpmName = request.getParameter("cpmName");
    response.getOutputStream().println(PASJava.addSafe(safeName, cpmName));
  } // add safe


  // +++++++++++++++++++++++++++++++++++++++++
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
    String safeName = request.getParameter("safeName");
    response.getOutputStream().println(PASJava.deleteSafe(safeName));
  } // delete safe

} // SafeServlet
