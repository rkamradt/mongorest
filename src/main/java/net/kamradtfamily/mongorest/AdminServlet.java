/*
 * Copyright Andrei Goumilevski
 * This file licensed under GPLv3 for non commercial projects
 * GPLv3 text http://www.gnu.org/licenses/gpl-3.0.html
 * For commercial usage please contact me
 * gmlvsk2@gmail.com
 *
*/

package net.kamradtfamily.mongorest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.*;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;

import com.mongodb.DB;
import com.mongodb.WriteResult;
import java.util.logging.Level;

import java.util.logging.Logger;

@SuppressWarnings("serial")
@WebServlet(name="AdminServlet")
public class AdminServlet extends SkeletonMongodbServlet {
  
  private static final Logger log = Logger.getLogger( AdminServlet.class.getName() );

  // --------------------------------
  @Override 
  public void init() throws ServletException{

    ServletConfig config = getServletConfig();
    String name = getServletName();
    log.log( Level.FINE, "init() {0}", name);

  }

  // --------------------------------
  @Override 
  public void destroy(){

    ServletConfig config = getServletConfig();
    String name = getServletName();
    log.log( Level.FINE, "destroy() {0}", name);

  }

  // DELETE
  // ------------------------------------
  @Override 
  protected void doDelete(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    log.fine( "doDelete()" );

    String db_name = req.getParameter( "dbname" );
    String user = req.getParameter( "user" );
    if( db_name==null || user==null ){
      error( res, SC_BAD_REQUEST, Status.get("param name missing") );
      return;
    }

    DB db = mongo.getDB( db_name );
    WriteResult o = db.removeUser( user );

    out_str( req, o.toString(), "application/json" );

  } 

  // PUT
  // ------------------------------------
  @Override 
  protected void doPut(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    log.fine( "doPut()" );

    String db_name = req.getParameter( "dbname" );
    if( db_name==null){
      error( res, SC_BAD_REQUEST, Status.get("param name missing") );
      return;
    }
    boolean read_only = Boolean.parseBoolean( req.getParameter("readonly") );

    DB db = mongo.getDB( db_name );
    WriteResult o = db.addUser("me","me".toCharArray());

    out_str( req, o.toString(), "application/json" );

  } 


}
