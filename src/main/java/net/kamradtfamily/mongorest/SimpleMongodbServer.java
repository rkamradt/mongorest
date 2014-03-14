/*
 * Copyright Andrei Goumilevski
 * This file licensed under GPLv3 for non commercial projects
 * GPLv3 text http://www.gnu.org/licenses/gpl-3.0.html
 * For commercial usage please contact me
 * gmlvsk2@gmail.com
 *
*/

package net.kamradtfamily.mongorest;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class SimpleMongodbServer {

  private static final Logger log = Logger.getLogger( SimpleMongodbServer.class.getName() );

  // -------------------------------------------
  public static void main(String[] args) throws Exception {

    Config.init();

    // server
    Server server = new Server(Config.server_port);
    

    //HandlerCollection handlers = new HandlerList();
    AbstractHandler _handler;


    ServletContextHandler context_handler = new ServletContextHandler( ServletContextHandler.NO_SESSIONS );
    context_handler.setContextPath( "/" );
    _handler = context_handler;

    server.setHandler( _handler );

    Map<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
    servlets.put( "/database/*", new DatabasesServlet() );
    servlets.put( "/collection/*", new CollectionsServlet() );
    servlets.put( "/index/*", new IndexServlet() );
    servlets.put( "/query/*", new QueryServlet() );
    servlets.put( "/user/*", new AdminServlet() );
    servlets.put( "/distinct/*", new DistinctServlet() );
    servlets.put( "/err", new ErrorServlet() );
    if( Config.gridfs )
      servlets.put( "/gridfs/*", new GridfsServlet() );
    //servlets.put( "/exc", new MyExceptionServlet() );

    // servlets
    for( String path:servlets.keySet() ){
      context_handler.addServlet( new ServletHolder(servlets.get(path)), path );
    }
    context_handler.addServlet( DefaultServlet.class, "/*" );

    // output filter
    FilterHolder out_filter = new FilterHolder( new OutputFilter() );
    context_handler.addFilter( out_filter, "/*", EnumSet.of(DispatcherType.REQUEST) );

    // input filter
    /*
    FilterHolder input_filter = new FilterHolder( new InputFilter() );
    context_handler.addFilter( input_filter, "/*", EnumSet.of(DispatcherType.REQUEST) );
    */

    Runnable shutdown = new Runnable(){
      @Override
      public void run(){
	log.info( "Shutting server" );
        MongoDB.close();
      }
    };
    Runtime.getRuntime().addShutdownHook( new Thread(shutdown) );

    server.start();
    server.join();

  }

}
