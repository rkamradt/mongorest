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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.http.*;
import static javax.servlet.http.HttpServletResponse.*;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import java.util.Date;
import java.util.Arrays;

import java.util.Collections;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.google.gson.Gson;


@SuppressWarnings("serial")
@WebServlet(name="MyExceptionServlet")
public class MyExceptionServlet extends HttpServlet { 

  private static final Logger log = Logger.getLogger( MyExceptionServlet.class.getName() );
  private Gson gson = new Gson();

  // --------------------------------
  @Override 
  public void init() throws ServletException{
    log.fine( "inited" );
    super.init();
  }

  // ------------------------------------
  @SuppressWarnings( "unchecked" )
  @Override 
  protected void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    Class exception_type = (Class)req.getAttribute( "javax.servlet.error.exception_type" );
    if( exception_type==null ){
      res.setStatus( SC_NOT_FOUND );
      return;
    }

    Integer status_code = (Integer)req.getAttribute( "javax.servlet.error.status_code" );
    String error_message = (String)req.getAttribute( "javax.servlet.error.message" );
    String request_uri = (String)req.getAttribute( "javax.servlet.error.request_uri" );
    Throwable exception = (Throwable)req.getAttribute( "javax.servlet.error.exception" );
    String servlet_name = (String)req.getAttribute( "javax.servlet.error.servlet_name" );


    log.severe( request_uri+": "+exception );

    MyException exc = (MyException)exception;

    res.setContentType( "application/json;charset=UTF-8" );
    res.setStatus( exc.code );

    Status st = exc.status;
    if( st==null )
      st = Status.FAIL;

    String s = gson.toJson( st );
    PrintWriter w = res.getWriter();
    w.println( s );
    w.flush();

  }

} 
