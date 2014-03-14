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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

import java.util.logging.Logger;
import java.util.logging.Level;

@SuppressWarnings("serial")
@WebServlet(name="QueryServlet")
public class QueryServlet extends SkeletonMongodbServlet {
  
  private final static int MAX_FIELDS_TO_RETURN = 10000;
  private static final Logger log = Logger.getLogger( QueryServlet.class.getName() );
  private final ThreadLocal<StringBuilder> tl = new ThreadLocal<StringBuilder>(){
    @Override 
    protected synchronized StringBuilder initialValue(){
      return new StringBuilder( 1024*4 );
    }
  };
  private final boolean rm_id = Config.mongo_remove_idfield;

  // --------------------------------
  @Override 
  public void init() throws ServletException{

    super.init();
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

    // server auth
    String ret;

    InputStream is = req.getInputStream();
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
    String skip = req.getParameter( "skip" );
    String limit = req.getParameter( "limit" );

    String _fields = req.getParameter( "fields" );
    String fields[] = null;
    if( _fields!=null )
      fields = _fields.split( "[,]" );

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

    StringBuilder buf = tl.get();
    // reset buf
    buf.setLength( 0 );

    BufferedReader r = null;
    DBObject q=null, sort=null;

    try{

      r = new BufferedReader( new InputStreamReader(is) ); 
      String data = r.readLine();
      if( data==null ){
	error( res, SC_BAD_REQUEST, Status.get("no data") );
	return;
      }
      try{
	q = (DBObject)JSON.parse( data );
	if( cache!=null ){
	  buf.append( db_name );
	  buf.append( col_name );
	  buf.append( data );
	}
      }
      catch( JSONParseException e ){
	error( res, SC_BAD_REQUEST, Status.get("can not parse data") );
	return;
      }
      // sort param
      data = r.readLine();
      if( data!=null ){
	try{
	  sort = (DBObject)JSON.parse( data );
	}
	catch( JSONParseException e ){
	  error( res, SC_BAD_REQUEST, Status.get("can not parse sort arg") );
	  return;
	}
	if( cache!=null )
	  buf.append( data );
      }

    }
    finally{
      if( r!=null )
	r.close();
    }

    // limit
    int lim;
    if( limit!=null ){
      try{
	lim =  Math.min(Integer.parseInt(limit), MAX_FIELDS_TO_RETURN);
      }catch( NumberFormatException e ){
	error( res, SC_BAD_REQUEST, Status.get("can not parse limit") );
	return;
      }
    }
    else{
      lim = MAX_FIELDS_TO_RETURN;
    }
    if( cache!=null ){
      buf.append( ";limit=" );
      buf.append( lim );
    }

    // skip
    int sk = -1; 
    if( skip!=null ){
      try{
	sk = Integer.parseInt( skip );
	if( cache!=null ){
	  buf.append( ";skip=" );
	  buf.append( sk );
	}
      }catch( NumberFormatException e ){
	error( res, SC_BAD_REQUEST, Status.get("can not parse skip") );
	return;
      }
    }

    // fields
    if( cache!=null && _fields!=null ){
      buf.append( ";fields=" );
      buf.append( _fields );
    }

    // from cache
    String cache_key = null;
    if( cache!=null ){
      cache_key = buf.toString();
      try{
	ret = (String)cache.get( cache_key );
      }
      // some wrong char in key
      catch( IllegalArgumentException e ){
	int l = buf.length();
	for( int i=0; i<l; i++ ){
	  if( buf.charAt(i)==' ' )
	    buf.setCharAt( i, '*' );
	}
	cache_key = buf.toString();
	ret = (String)cache.get( cache_key );
      }
      if( ret!=null ){
	out_str( req, ret, "application/json" );
	return;
      }
    }

    // cursor
    DBCursor c;
    if( fields!=null ){
      StringBuilder sb = new StringBuilder();
      sb.append( "{" );
      int len = fields.length;
      for( int i=0; i<len; i++ ){
	String s = fields[i];
	sb.append( '"' );
	sb.append( s );
	sb.append( '"' );
	sb.append( ":1" );
	if( i!=(len-1) )
	  sb.append( "," );
      }
      sb.append( "}" );
      c = col.find( q, (DBObject)JSON.parse( sb.toString() ) );
    }
    else
      c = col.find( q );

    if( c==null || c.count()==0 ){
      error( res, SC_NOT_FOUND, Status.get("no documents found") );
      return;
    }

    if( sort!=null )
      c.sort( sort );

    res.setIntHeader( "X-Documents-Count", c.count() );

    c.limit( lim );
    if( sk!=-1 )
      c.skip( sk );

    // reset buf
    buf.setLength( 0 );

    int no = 0;
    buf.append( "[" );
    while( c.hasNext() ){

      DBObject o = c.next();
      if( rm_id )
	o.removeField( "_id" );
      JSON.serialize( o, buf );
      buf.append( "," );
      no++;

    }
    c.close();

    if( no>0 )
      buf.setCharAt( buf.length()-1, ']' );
    else
      buf.append( ']' );

    res.setIntHeader( "X-Documents-Returned", no );

    ret = buf.toString();
    if( cache!=null )
      cache.set( cache_key, ret );

    out_str( req, ret, "application/json" );

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
    String skip = req.getParameter( "skip" );
    String limit = req.getParameter( "limit" );

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

    DBCursor c = col.find();
    if( c==null || c.count()==0 ){
      error( res, SC_NOT_FOUND, Status.get("no documents found") );
      return;
    }

    res.setIntHeader( "X-Documents-Count", c.count() );

    if( limit!=null ){
      try{
	c.limit( Math.min(Integer.parseInt(limit), MAX_FIELDS_TO_RETURN) );
      }catch( NumberFormatException e ){
	error( res, SC_BAD_REQUEST, Status.get("can not parse limit") );
	c.close();
	return;
      }
    }
    else
      c.limit( MAX_FIELDS_TO_RETURN );

    if( skip!=null ){
      try{
	c.skip( Integer.parseInt(skip) );
      }catch( NumberFormatException e ){
	error( res, SC_BAD_REQUEST, Status.get("can not parse skip") );
	c.close();
	return;
      }
    }

    StringBuilder buf = tl.get();
    buf.setLength( 0 );

    int no = 0;
    buf.append( "[" );
    while( c.hasNext() ){

      DBObject o = c.next();
      if( rm_id )
	o.removeField( "_id" );
      JSON.serialize( o, buf );
      buf.append( "," );
      no++;

    }
    c.close();

    if( no>0 )
      buf.setCharAt( buf.length()-1, ']' );
    else
      buf.append( ']' );

    res.setIntHeader( "X-Documents-Returned", no );

    out_str( req, buf.toString(), "application/json" );

  } 

  // -------------------------------------------------
  // For cross-origin requests
  /*
  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    log.fine( "options" );

    Header h = req.getFirstHeader( "Access-Control-Request-Headers" );
    h = req.getFirstHeader( "Access-Control-Request-Method" );
    h = req.getFirstHeader( "Origin" );

    res.setHeader( "Access-Control-Allow-Origin", "*" );
    res.setHeader( "Access-Control-Allow-Headers", "authorization" );
    res.setHeader( "Access-Control-Allow-Methods", "GET,POST,OPTIONS" );

    res.setStatus( SC_OK );

  }
  */

}
