/*
 * Copyright Andrei Goumilevski
 * This file licensed under GPLv3 for non commercial projects
 * GPLv3 text http://www.gnu.org/licenses/gpl-3.0.html
 * For commercial usage please contact me
 * gmlvsk2@gmail.com
 *
*/

package net.kamradtfamily.mongorest;

import java.util.logging.Logger;
import java.util.Properties;
import java.util.Map;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

public class Config {

  private static final Logger log = Logger.getLogger( Config.class.getName() );

  // mongo server
  public static String mongo_servers = "127.0.0.1:27017";
  public static boolean mongo_safeoperations = false;
  public static boolean mongo_remove_idfield = false;

  // main server
  public static int server_threadsno = 50;
  public static int server_port = 8080;
  public static String server_adr = null;


  // memcached server
  public static boolean memcached = false;
  public static String memcached_servers = null;
  public static int memcached_expire = 0;

  // search
  public static boolean search = false;
  public static String search_index_path = null;
  public static Map<String,ArrayList<String>> search_index_fields = null;
  public static String search_default_field = null;

  // gridfs
  public static boolean gridfs = false;

  private static final int MINTHRNO = 3;

  // -------------------------------------------
  public static int i( String s, int i ){
    if( s==null )
      return i;
     try{
       return Integer.parseInt( s );
     }catch( NumberFormatException e ){
       return i;
     }
  }

  // -------------------------------------------
   static void init(){

     Properties prop;

     try{

       prop = new Properties();
       InputStream in = new FileInputStream( "/mongoser.properties" );
       prop.load( in );
       in.close();

     }
     catch( IOException e ){
       return;
     }

     // mongodb
     mongo_servers = prop.getProperty( "mongo.servers", "127.0.0.1:27017" );
     if( mongo_servers==null )
       throw new IllegalArgumentException( "mongo.servers missing" );
     mongo_safeoperations = Boolean.parseBoolean( prop.getProperty("mongo.safeoperations", "false") );
     mongo_remove_idfield = Boolean.parseBoolean( prop.getProperty("mongo.remove.idfield", "false") );

     // rest server
     server_threadsno = i( prop.getProperty("server.threadsno"), 50 );
     if( server_threadsno<MINTHRNO ){
       server_threadsno = MINTHRNO;
       log.log( Level.WARNING, "Changed threads no to {0}", server_threadsno);
     }
     server_port = i( prop.getProperty("server.port"), 8080 );
     server_adr = prop.getProperty( "server.adr", null );

     // ssl
     // memcached
     memcached = Boolean.parseBoolean( prop.getProperty("memcached", "false") );
     if( memcached ){
       memcached_servers = prop.getProperty( "memcached.servers", "127.0.0.1:11211" );
       memcached_expire = i( prop.getProperty("memcached.expire"), 0 );
     }

     // gridfs
     gridfs = Boolean.parseBoolean( prop.getProperty("gridfs", "false") );

   }


}
