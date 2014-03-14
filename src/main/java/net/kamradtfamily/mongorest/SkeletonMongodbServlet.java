/*
 * Copyright Andrei Goumilevski
 * This file licensed under GPLv3 for non commercial projects
 * GPLv3 text http://www.gnu.org/licenses/gpl-3.0.html
 * For commercial usage please contact me
 * gmlvsk2@gmail.com
 *
*/

package net.kamradtfamily.mongorest;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.*;
import javax.servlet.ServletConfig;

import com.mongodb.Mongo;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.logging.Level;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.logging.Logger;

@SuppressWarnings("serial")
public class SkeletonMongodbServlet extends HttpServlet {
  
  private static final Logger log = Logger.getLogger( SkeletonMongodbServlet.class.getName() );

  protected Mongo mongo = MongoDB.get();
  protected MyCache cache = null;
  static final Pattern pattern = Pattern.compile( "^/([^/]+)(?:/([^/]+))?/?$" );


  // --------------------------------
  protected void out_xml( HttpServletRequest req, Object o ){
    req.setAttribute( "what", OutputFilter.XML );
    req.setAttribute( "value", o );
  }

  // --------------------------------
  protected void out_json( HttpServletRequest req, Object o ){
    req.setAttribute( "what", OutputFilter.JSON );
    req.setAttribute( "value", o );
  }

  // --------------------------------
  protected void out_str( HttpServletRequest req, String s, String content_type ){
    req.setAttribute( "type", content_type );
    out_str( req, s );
  }

  // --------------------------------
  protected void out_str( HttpServletRequest req, String s ){
    req.setAttribute( "what", OutputFilter.STR );
    req.setAttribute( "value", s );
  }

  // --------------------------------
  protected void out( HttpServletRequest req ){
    req.setAttribute( "what", OutputFilter.EMPTY );
  }

  // --------------------------------
  protected void check_null( Object o ){

    if( o==null )
      throw new MyException( SC_BAD_REQUEST );

  }

  // --------------------------------
  String[] req2mongonames( HttpServletRequest req ){

    String pi = req.getPathInfo();
    if( pi==null )
      return null;

    Matcher m = pattern.matcher( pi );

    String paths[] = null;

    if( m.find() ){
      paths = new String[2];
      paths[0] = m.group( 1 );
      paths[1] = m.group( 2 );
    }

    return paths;

  }

  // --------------------------------
  protected void error( HttpServletResponse res, int code, Status st )
    throws IOException {

    res.setContentType( "application/json;charset=UTF-8" );
    res.setStatus( code );
    PrintWriter w = res.getWriter();
    w.println( Status.to_json(st) );
    w.flush();

  }

  // --------------------------------
  protected String get_param( HttpServletRequest req, String key ){

    String v = req.getParameter( key );
    if( v==null || v.length()==0 )
      throw new MyException( SC_BAD_REQUEST, Status.get("missing param:"+key) );

    return v;

  }

  // --------------------------------
  @Override 
  public void init() throws ServletException{

    ServletConfig config = getServletConfig();
    String name = getServletName();
    log.log( Level.FINE, "default init() {0}", name);

    if( Config.memcached ){
      cache = MyCache.get();
    }

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
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    log.fine( "default doPost()" );
    resp.sendError(  SC_METHOD_NOT_ALLOWED );

  } 

  // GET
  // ------------------------------------
  @Override 
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    log.fine( "default doGet()" );
    resp.sendError(  SC_METHOD_NOT_ALLOWED );

  } 

  // DELETE
  // ------------------------------------
  @Override 
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    log.fine( "default doDelete()" );
    resp.sendError(  SC_METHOD_NOT_ALLOWED );

  } 

  // HEAD
  // ------------------------------------
  @Override 
  protected void doHead(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    log.fine( "default doHead()" );
    resp.sendError(  SC_METHOD_NOT_ALLOWED );

  } 

  // OPTIONS
  // ------------------------------------
  @Override 
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    log.fine( "default doOptions()" );
    resp.sendError(  SC_METHOD_NOT_ALLOWED );

  } 

  // PUT
  // ------------------------------------
  @Override 
  protected void doPut(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    log.fine( "default doPut()" );
    resp.sendError(  SC_METHOD_NOT_ALLOWED );

  } 

  // TRACE
  // ------------------------------------
  @Override 
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    log.fine( "default doTrace()" );
    resp.sendError(  SC_METHOD_NOT_ALLOWED );

  } 

}
