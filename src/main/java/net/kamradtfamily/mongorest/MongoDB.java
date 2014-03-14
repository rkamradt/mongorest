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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

import java.net.UnknownHostException;

import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

public class MongoDB {
  
  private static final Logger log = Logger.getLogger( MongoDB.class.getName() );

  // --------------------------------
  private static List<ServerAddress> str2addresses( String servers )
    throws UnknownHostException {

    List<ServerAddress> list = new ArrayList<ServerAddress>();
    servers = servers.trim();
    String ar[] = servers.split( "[,]" );
    for( String s:ar ){
      s = s.trim();
      String ar2[] = s.split( "[:]" );
      if( ar2==null || ar2.length!=2 )
	throw new IllegalArgumentException( "mongo server must be <server:port>" );
      int port = 27017;
      try{
	port = Integer.parseInt( ar2[1] );
      } catch( NumberFormatException e ){ log.warning( "Can not parse port for "+ar[0] ); }
      ServerAddress adr = new ServerAddress( ar2[0] , port );
      list.add( adr );
    }

    return list;

  }

  private static Mongo db = null;
  static WriteConcern write_concern;
  // --------------------------------
  public static Mongo get(){

    if( db==null ){
      try{
	List<ServerAddress> addrs = str2addresses( Config.mongo_servers );
	MongoOptions opts = new MongoOptions();
	opts.autoConnectRetry = true;
	int thrno = Config.server_threadsno;
	if( thrno<100 )
	  opts.connectionsPerHost = thrno;
	else
	  opts.connectionsPerHost = 100;
	opts.threadsAllowedToBlockForConnectionMultiplier = 10;
	opts.maxWaitTime = 10000; // millisecs
	db = new Mongo( addrs, opts );
	write_concern = Config.mongo_safeoperations?WriteConcern.FSYNC_SAFE:WriteConcern.NORMAL;
	log.info( "getDB():"+db );
      }
      catch( UnknownHostException e ){
	log.severe( "Bad host "+e );
	db = null;
      }
    }

    return db;

  }

  // --------------------------------
  public static void close(){
    if( db!=null )
      db.close();
    db = null;
  }

}
