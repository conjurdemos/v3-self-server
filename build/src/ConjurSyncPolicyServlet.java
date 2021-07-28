/*
 * Defines REST endpoints to:
 * - create a synchronization policy for a safe
 * - delete a synchronization policy for a safe
 */

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// ###########################################
// POST = create a synchronization policy for a safe
// DELETE = delete a synchronization policy for a safe

public class ConjurSyncPolicyServlet extends HttpServlet {
    /** Logger */
    private static final Logger logger = Logger.getLogger(ConjurSyncPolicyServlet.class.getName());

  // +++++++++++++++++++++++++++++++++++++++++
  // Create a synchronization policy for a safe and loads at root
  // This defines the sync policy ahead of the synchronizer
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
    String vaultName = request.getParameter("vaultName");
    String lobName = request.getParameter("lobName");
    String safeName = request.getParameter("safeName");

    // generate policy - REST method accepts text - no need to create a file
    String policyText = "---\n"
                        + "- !policy\n"
                        + "  id: " + vaultName + "\n"
                        + "  body:\n"
                        + "  - !group " + lobName + "-admins\n"
                        + "  - !policy\n"
                        + "    id: " + lobName + "\n"
                        + "    owner: !group /" + vaultName + "/" + lobName + "-admins\n"
                        + "    body:\n"
                        + "    - !group " + safeName + "-admins\n"
                        + "    - !policy\n"
                        + "      id: " + safeName + "\n"
                        + "      body:\n"
                        + "      - !policy\n"
                        + "        id: delegation\n"
                        + "        owner: !group /" + vaultName + "/" + lobName + "/" + safeName + "-admins\n"
                        + "        body:\n"
                        + "        - !group consumers\n";

    logger.log(Level.INFO, "Appending synch policy: " + policyText + " at root.");
    String loadPolicyResponse = ConjurJava.loadPolicy("append", "root", policyText);
    response.getOutputStream().println(loadPolicyResponse);
  }

  // +++++++++++++++++++++++++++++++++++++++++
  // Deletes a synchronization policy for a safe
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
    String vaultName = request.getParameter("vaultName");
    String lobName = request.getParameter("lobName");
    String safeName = request.getParameter("safeName");
    String policyText = "---\n"
                        + "- !delete\n"
                        + "  record: !group " + vaultName
                        + "/" + lobName
                        + "/" + safeName
                        + "/delegation/consumers"
                        + "\n"
                        + "- !delete\n"
                        + "  record: !group " + vaultName
                        + "/" + lobName
                        + "/" + safeName
                        + "-admins"
                        + "\n"
                        + "- !delete\n"
                        + "  record: !policy " + vaultName
                        + "/" + lobName
                        + "/" + safeName;
    logger.log(Level.INFO, "Deleting sync policy with: " + policyText + " at root.");
    String delPolicyResponse = ConjurJava.loadPolicy("delete", "root", policyText);

    response.getOutputStream().println(delPolicyResponse);
  }

} // ConjurSyncPolicyServlet
