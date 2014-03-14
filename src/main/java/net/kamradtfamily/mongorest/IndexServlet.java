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
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;


import java.util.logging.Logger;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("serial")
@WebServlet(name="IndexServlet")
public class IndexServlet extends SkeletonMongodbServlet {
  
  private static final Logger log = Logger.getLogger( IndexServlet.class.getName() );

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

    InputStream is = req.getInputStream();
    String db_name = req.getParameter( "dbname" );
    String col_name = req.getParameter( "colname" );
    if( db_name==null || col_name==null ){
      error( res, SC_BAD_REQUEST, Status.get("param name missing") );
      return;
    }

    DB db = mongo.getDB( db_name );

    // mongo auth
    String user = req.getParameter( "user" );
    String passwd = req.getParameter( "passwd" );
    if( user!=null&&passwd!=null&&(!db.isAuthenticated()) ){
      boolean auth = db.authenticate( user, passwd.toCharArray() );
      if( !auth ){
	res.sendError( SC_UNAUTHORIZED );
	return;
      }
    }

    DBCollection col = db.getCollection( col_name );

    BufferedReader r = null;
    String data = null;

    try{

      r = new BufferedReader( new InputStreamReader(is) ); 
      data = r.readLine();

    }
    finally{
      if( r!=null )
	r.close();
    }
    if( data==null ){
      error( res, SC_BAD_REQUEST, Status.get("no data") );
      return;
    }

    DBObject o;
    try{
      o = (DBObject)JSON.parse( data );
    }
    catch( JSONParseException e ){
      error( res, SC_BAD_REQUEST, Status.get("can not parse data") );
      return;
    }

    col.createIndex( o );

    res.setStatus( SC_CREATED );

  } 

  // DELETE
  // ------------------------------------
  @Override 
  protected void doDelete(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    log.fine( "doDelete()" );

    InputStream is = req.getInputStream();
    String db_name = req.getParameter( "dbname" );
    String col_name = req.getParameter( "colname" );
    if( db_name==null || col_name==null ){
      error( res, SC_BAD_REQUEST, Status.get("param name missing") );
      return;
    }

    BufferedReader r = null;
    String data = null;

    try{

      r = new BufferedReader( new InputStreamReader(is) ); 
      data = r.readLine();

    }
    finally{
      if( r!=null )
	r.close();
    }
    if( data==null ){
      error( res, SC_BAD_REQUEST, Status.get("no data") );
      return;
    }

    DBObject o;
    try{
      o = (DBObject)JSON.parse( data );
    }
    catch( JSONParseException e ){
      error( res, SC_BAD_REQUEST, Status.get("can not parse data") );
      return;
    }

    DB db = mongo.getDB( db_name );

    // mongo auth
    String user = req.getParameter( "user" );
    String passwd = req.getParameter( "passwd" );
    if( user!=null&&passwd!=null&&(!db.isAuthenticated()) ){
      boolean auth = db.authenticate( user, passwd.toCharArray() );
      if( !auth ){
	res.sendError( SC_UNAUTHORIZED );
	return;
      }
    }

    DBCollection col = db.getCollection( col_name );

    col.dropIndex( o );

    res.setStatus( SC_OK );

  } 

  // GET
  // ------------------------------------
  @Override 
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    log.fine( "doGet()" );

    String db_name = req.getParameter( "dbname" );
    String col_name = req.getParameter( "colname" );
    if( db_name==null || col_name==null ){
      error( res, SC_BAD_REQUEST, Status.get("param name missing") );
      return;
    }

    DB db = mongo.getDB( db_name );

    // mongo auth
    String user = req.getParameter( "user" );
    String passwd = req.getParameter( "passwd" );
    if( user!=null&&passwd!=null&&(!db.isAuthenticated()) ){
      boolean auth = db.authenticate( user, passwd.toCharArray() );
      if( !auth ){
	res.sendError( SC_UNAUTHORIZED );
	return;
      }
    }

    DBCollection col = db.getCollection( col_name );

    List<DBObject> info = col.getIndexInfo();
    out_json( req, info );

  } 

}
