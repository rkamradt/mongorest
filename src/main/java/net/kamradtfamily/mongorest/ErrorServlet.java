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
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import java.util.Date;
import java.util.Arrays;
import javax.servlet.annotation.WebServlet;

import java.util.Collections;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.io.OutputStream;
import java.io.PrintWriter;


@SuppressWarnings("serial")
@WebServlet(name="ErrorServlet")
public class ErrorServlet extends HttpServlet { 

  private static final Logger log = Logger.getLogger( ErrorServlet.class.getName() );

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

    // Nothing

  }

} 
