/*
 * Copyright Andrei Goumilevski
 * This file licensed under GPLv3 for non commercial projects
 * GPLv3 text http://www.gnu.org/licenses/gpl-3.0.html
 * For commercial usage please contact me
 * gmlvsk2@gmail.com
 *
*/

package net.kamradtfamily.mongorest;

import javax.servlet.http.*;
import javax.servlet.*;
import java.util.logging.Logger;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import static javax.servlet.http.HttpServletResponse.*;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;

public class OutputFilter implements Filter {

  private static final Logger log = Logger.getLogger( OutputFilter.class.getName() );

  public static final int EMPTY = 0;
  public static final int STR = 1;
  public static final int JSON = 2;
  public static final int XML = 3;

  private Gson gson = new Gson();

  // --------------------------------
  public void init( FilterConfig config ){
    log.fine( "start OutputFilter" );
  }

  // --------------------------------
  public void doFilter( ServletRequest _req, ServletResponse _res, FilterChain chain )
    throws ServletException, IOException {

    chain.doFilter( _req, _res );

    log.fine( "doFilter()" );

    HttpServletRequest req = (HttpServletRequest)_req;
    HttpServletResponse res = (HttpServletResponse)_res;

    res.setHeader( "Cache-Control", "no-cache,no-store,must-revalidate" );

    final Integer what = (Integer)req.getAttribute( "what" );
    if( what==null ){
      return;
    }

    switch( what ){

      case EMPTY:{
        res.setStatus( SC_OK );
      }
      break;

      case STR:{
	String content_type = (String)req.getAttribute( "type" );
	if( content_type!=null )
	  res.setContentType( content_type+";charset=UTF-8" );
	else
	  res.setContentType( "html/text;charset=UTF-8" );
        res.setStatus( SC_OK );
	Object o = req.getAttribute( "value" );
	if( o!=null ){
	  PrintWriter w = res.getWriter();
	  w.println( o );
	  w.flush();
	}
      }
      break;

      case JSON:{
	res.setContentType( "application/json;charset=UTF-8" );
        res.setStatus( SC_OK );
	Object o = req.getAttribute( "value" );
	if( o!=null ){
	  String s = gson.toJson( o );
	  PrintWriter w = res.getWriter();
	  w.println( s );
	  w.flush();
	}
      }
      break;

      case XML:{
	res.setContentType( "application/xml;charset=UTF-8" );
        res.setStatus( SC_OK );
      }
      break;

    }

  }

  // --------------------------------
  public void destroy(){
    log.fine( "stop OutputFilter" );
  }

}
