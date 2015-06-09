package wookie.yql.analytics

import scala.math._

/**
 * @author lukaszjastrzebski
 */
object Test extends App {
  
//  val R = 6371000
//
//  val fi1 = toRadians(32.8310899)
//  val fi2 = toRadians(37.341595)
//  val dfi = (abs(fi1 - fi2))
//  val dalfa = abs(toRadians(-121.9955771) - toRadians(-116.92844))
//  
//  val a = pow(sin(dfi/2), 2) + cos(fi1) * cos(fi2) * pow(sin(dalfa / 2.0), 2)
//  val c = 2 * atan2( sqrt(a), sqrt(1 - a) )
//  val d = R * c
//  
//  println(d / 1000)
  
  
  val  dist = 150.0/6371.0;
val  brng = Math.toRadians(90);
val  lat1 = Math.toRadians(26.88288045572338);
val  lon1 = Math.toRadians(75.78369140625);

val  lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(brng) );
val  a = Math.atan2(Math.sin(brng)*Math.sin(dist)*Math.cos(lat1), Math.cos(dist)-Math.sin(lat1)*Math.sin(lat2));
System.out.println("a = " +  a);
var  lon2 = lon1 + a;

lon2 = (lon2+ 3*Math.PI) % (2*Math.PI) - Math.PI;

System.out.println("Latitude = "+Math.toDegrees(lat2)+"\nLongitude = "+Math.toDegrees(lon2));

}