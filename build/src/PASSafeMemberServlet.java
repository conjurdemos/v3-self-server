/*
 * Defines REST endpoints to:
 * - list members of a safe
 * - add member to a safe
 * - delete member from a safe
 */

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// ###########################################
// GET = list members
// POST = add member safe
// DELETE = remove member

public class PASSafeMemberServlet extends HttpServlet {
    /** Logger */
    private static final Logger logger = Logger.getLogger(PASSafeMemberServlet.class.getName());


  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
    String safeName = request.getParameter("safeName");
    response.getOutputStream().println(PASJava.getSafeMembers(safeName));
  } // list safes

  // +++++++++++++++++++++++++++++++++++++++++
  // add member to safe
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
    String safeName = request.getParameter("safeName");
    String memberName = request.getParameter("memberName");
		
    String addSafeMemberResponse = PASJava.addSafeMember(safeName, memberName);
    response.getOutputStream().println(addSafeMemberResponse);
  } // add safe


  // +++++++++++++++++++++++++++++++++++++++++
  // remove member from safe
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
    String safeName = request.getParameter("safeName");
    String memberName = request.getParameter("memberName");
		
    String removeSafeMemberResponse = PASJava.removeSafeMember(safeName, memberName);
    response.getOutputStream().println(removeSafeMemberResponse);
  } // delete safe

} // SafeServlet
