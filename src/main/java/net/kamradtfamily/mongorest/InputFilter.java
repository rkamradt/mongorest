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
import static javax.servlet.http.HttpServletResponse.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;

public class InputFilter implements Filter {

  private static final Logger log = Logger.getLogger( InputFilter.class.getName() );


  // --------------------------------
  public void init( FilterConfig config ){
    log.fine( "start InputFilter" );
  }

  // --------------------------------
  public void doFilter( ServletRequest _req, ServletResponse _res, FilterChain chain )
    throws ServletException, IOException {

    log.fine( "doFilter()" );

    HttpServletRequest req = (HttpServletRequest)_req;
    HttpServletResponse res = (HttpServletResponse)_res;

    chain.doFilter( req, res );

  }

  // --------------------------------
  public void destroy(){
    log.fine( "stop InputFilter" );
  }

}
