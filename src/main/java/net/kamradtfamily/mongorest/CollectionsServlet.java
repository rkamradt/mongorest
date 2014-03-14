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
import com.mongodb.DBCollection;
import org.bson.BasicBSONObject;

import java.util.logging.Logger;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("serial")
@WebServlet(name="CollectionsServlet")
public class CollectionsServlet extends SkeletonMongodbServlet {
  
  private static final Logger log = Logger.getLogger( CollectionsServlet.class.getName() );

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

  // PUT
  // ------------------------------------
  @Override 
  protected void doPut(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    log.fine( "doPut()" );

    String db_name = req.getParameter( "dbname" );
    String col_name = req.getParameter( "colname" );
    if( db_name==null || col_name==null ){
      String names[]  = req2mongonames( req );
      if( names!=null ){
	db_name = names[0];
	col_name = names[1];
      }
      if( db_name==null || col_name==null ){
	error( res, SC_BAD_REQUEST, Status.get("param name missing") );
	return;
      }
    }

    DB db = mongo.getDB( db_name );
    db.createCollection( col_name, null );

    res.setStatus( SC_CREATED );

  } 

  // DELETE
  // ------------------------------------
  @Override 
  protected void doDelete(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    log.fine( "doDelete()" );

    String db_name = req.getParameter( "dbname" );
    String col_name = req.getParameter( "colname" );
    if( db_name==null || col_name==null ){
      String names[]  = req2mongonames( req );
      if( names!=null ){
	db_name = names[0];
	col_name = names[1];
      }
      if( db_name==null || col_name==null ){
	error( res, SC_BAD_REQUEST, Status.get("param name missing") );
	return;
      }
    }

    DB db = mongo.getDB( db_name );
    DBCollection col = db.getCollection( col_name );
    col.drop();
    res.setStatus( SC_OK );

  } 

  // GET
  // ------------------------------------
  @Override 
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    log.fine( "doGet()" );

    String db_name = req.getParameter( "dbname" );
    String op = req.getParameter( "op" );
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

    if( op==null )
      op = "list";

    DB db = mongo.getDB( db_name );

    if( "list".equals(op) ){
      Set<String> cols = db.getCollectionNames();
      out_json( req, cols );
      return;
    }

    // requires colname
    String col_name = req.getParameter( "colname" );
    if( col_name==null ){
      error( res, SC_BAD_REQUEST, Status.get("param name missing") );
      return;
    }
    DBCollection col = db.getCollection( col_name );

    if( "count".equals(op) ){
      out_str( req, "{\"count\":"+col.count()+"}", "application/json" );
    }
    else if( "stats".equals(op) ){
      BasicBSONObject o = col.getStats();
      out_str( req, o.toString(), "application/json" );
    }
    else
      res.sendError(  SC_BAD_REQUEST );

  } 


}
