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
import org.bson.BasicBSONObject;

import java.util.logging.Logger;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("serial")
@WebServlet(name="DatabasesServlet")
public class DatabasesServlet extends SkeletonMongodbServlet {
  
  private static final Logger log = Logger.getLogger( DatabasesServlet.class.getName() );

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
    if( db_name==null ){
      String names[]  = req2mongonames( req );
      if( names!=null ){
	db_name = names[0];
      }
      if( db_name==null ){
	error( res, SC_BAD_REQUEST, Status.get("param name missing") );
	return;
      }
    }

    mongo.dropDatabase( db_name );
    out_json( req, Status.OK );

  } 

  // GET
  // ------------------------------------
  @Override 
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    log.fine( "doGet()" );

    String op = req.getParameter( "op" );
    if( op==null )
      op = "list";

    if( "list".equals(op) ){
      List<String> dbs = mongo.getDatabaseNames();
      out_json( req, dbs );
      return;
    }

    // requires dbname
    String db_name = req.getParameter( "dbname" );
    if( db_name==null ){
      String names[]  = req2mongonames( req );
      if( names!=null ){
	db_name = names[0];
      }
      if( db_name==null ){
	error( res, SC_BAD_REQUEST, Status.get("param name missing") );
	return;
      }
    }
    DB db = mongo.getDB( db_name );

    if( "stats".equals(op) ){
      BasicBSONObject o = db.getStats();
      out_str( req, o.toString(), "application/json" );
    }
    else if( "lasterror".equals(op) ){
      BasicBSONObject o = db.getLastError();
      out_str( req, o.toString(), "application/json" );
    }
    else if( "setreadonly".equals(op) ){
      String tmp = req.getParameter( "readonly" );
      if( tmp==null ){
	error( res, SC_BAD_REQUEST, Status.get("param name missing") );
	return;
      }
      boolean read_only = Boolean.parseBoolean( tmp );
      db.setReadOnly( read_only );
      out_json( req, Status.OK );
    }
    else
      res.sendError(  SC_BAD_REQUEST );

  } 


}
