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
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

import java.util.logging.Logger;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("serial")
@WebServlet(name="QueryServlet")
public class DistinctServlet extends SkeletonMongodbServlet {
  
  private final static int MAX_FIELDS_TO_RETURN = 1000;
  private static final Logger log = Logger.getLogger( DistinctServlet.class.getName() );
  private final ThreadLocal<StringBuilder> tl = new ThreadLocal<StringBuilder>(){
    @Override 
    protected synchronized StringBuilder initialValue(){
      return new StringBuilder( 1024*4 );
    }
  };

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

  // POST
  // ------------------------------------
  @Override 
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    log.fine( "doPost()" );

    InputStream is = req.getInputStream();
    String db_name = req.getParameter( "dbname" );
    String col_name = req.getParameter( "colname" );
    String key = req.getParameter( "key" );
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
    if( key==null ){
      error( res, SC_BAD_REQUEST, Status.get("param name missing") );
      return;
    }

    DB db = mongo.getDB( db_name );
    DBCollection col = db.getCollection( col_name );

    BufferedReader r = null;
    DBObject q = null;
    try{

      r = new BufferedReader( new InputStreamReader(is) ); 
      String data = r.readLine();
      if( data==null ){
	error( res, SC_BAD_REQUEST, Status.get("no data") );
	return;
      }
      try{
	q = (DBObject)JSON.parse( data );
      }
      catch( JSONParseException e ){
	error( res, SC_BAD_REQUEST, Status.get("can not parse data") );
	return;
      }

    }
    finally{
      if( r!=null )
	r.close();
    }

    List l = col.distinct( key, q );
    if( l==null || l.isEmpty() ){
      error( res, SC_NOT_FOUND, Status.get("no documents found") );
      return;
    }

    res.setIntHeader( "X-Documents-Count", l.size() );

    StringBuilder buf = tl.get();
    // reset buf
    buf.setLength( 0 );

    JSON.serialize( l, buf );
    out_str( req, buf.toString(), "application/json" );

  } 

  // GET
  // ------------------------------------
  @Override 
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    log.fine( "doGet()" );

    String db_name = req.getParameter( "dbname" );
    String col_name = req.getParameter( "colname" );
    String key = req.getParameter( "key" );
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
    if( key==null ){
      error( res, SC_BAD_REQUEST, Status.get("param name missing") );
      return;
    }

    DB db = mongo.getDB( db_name );
    DBCollection col = db.getCollection( col_name );

    List l = col.distinct( key );
    if( l==null || l.isEmpty() ){
      error( res, SC_NOT_FOUND, Status.get("no documents found") );
      return;
    }

    res.setIntHeader( "X-Documents-Count", l.size() );

    StringBuilder buf = tl.get();
    buf.setLength( 0 );

    JSON.serialize( l, buf );
    out_str( req, buf.toString(), "application/json" );

  } 
}
