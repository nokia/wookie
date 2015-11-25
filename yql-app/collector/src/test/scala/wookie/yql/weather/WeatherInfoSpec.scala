/* Copyright (C) 2014-2015 by Nokia.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package wookie.yql.weather

import _root_.org.specs2.mutable._
import _root_.org.junit.runner.RunWith
import _root_.org.specs2.runner.JUnitRunner
import org.http4s.Response
import scodec.bits.ByteVector
import wookie.collector.streams.JsonTransformer

import scalaz.concurrent.Task

@RunWith(classOf[JUnitRunner])
class WeatherInfoSpec extends Specification {

  import WeatherDecoders._

  val multiWeatherResponse = """
{
 "query": {
  "count": 2,
  "created": "2015-04-03T22:58:39Z",
  "lang": "en-US",
  "results": {
   "channel": [
    {
     "title": "Yahoo! Weather - Sunnyvale, CA",
     "link": "http://us.rd.yahoo.com/dailynews/rss/weather/Sunnyvale__CA/*http://weather.yahoo.com/forecast/USCA1116_f.html",
     "description": "Yahoo! Weather for Sunnyvale, CA",
     "language": "en-us",
     "lastBuildDate": "Fri, 03 Apr 2015 2:54 pm PDT",
     "ttl": "60",
     "location": {
      "city": "Sunnyvale",
      "country": "United States",
      "region": "CA"
     },
     "units": {
      "distance": "mi",
      "pressure": "in",
      "speed": "mph",
      "temperature": "F"
     },
     "wind": {
      "chill": "68",
      "direction": "350",
      "speed": "12"
     },
     "atmosphere": {
      "humidity": "39",
      "pressure": "30.15",
      "rising": "2",
      "visibility": "10"
     },
     "astronomy": {
      "sunrise": "6:50 am",
      "sunset": "7:32 pm"
     },
     "image": {
      "title": "Yahoo! Weather",
      "width": "142",
      "height": "18",
      "link": "http://weather.yahoo.com",
      "url": "http://l.yimg.com/a/i/brand/purplelogo//uh/us/news-wea.gif"
     },
     "item": {
      "title": "Conditions for Sunnyvale, CA at 2:54 pm PDT",
      "lat": "37.37",
      "long": "-122.04",
      "link": "http://us.rd.yahoo.com/dailynews/rss/weather/Sunnyvale__CA/*http://weather.yahoo.com/forecast/USCA1116_f.html",
      "pubDate": "Fri, 03 Apr 2015 2:54 pm PDT",
      "condition": {
       "code": "34",
       "date": "Fri, 03 Apr 2015 2:54 pm PDT",
       "temp": "68",
       "text": "Fair"
      },
      "description": "\n<img src=\"http://l.yimg.com/a/i/us/we/52/34.gif\"/><br />\n<b>Current Conditions:</b><br />\nFair, 68 F<BR />\n<BR /><b>Forecast:</b><BR />\nFri - Sunny. High: 69 Low: 45<br />\nSat - Mostly Sunny. High: 64 Low: 45<br />\nSun - Showers. High: 59 Low: 43<br />\nMon - Partly Cloudy. High: 62 Low: 48<br />\nTue - Rain. High: 56 Low: 44<br />\n<br />\n<a href=\"http://us.rd.yahoo.com/dailynews/rss/weather/Sunnyvale__CA/*http://weather.yahoo.com/forecast/USCA1116_f.html\">Full Forecast at Yahoo! Weather</a><BR/><BR/>\n(provided by <a href=\"http://www.weather.com\" >The Weather Channel</a>)<br/>\n",
      "forecast": [
       {
        "code": "32",
        "date": "3 Apr 2015",
        "day": "Fri",
        "high": "69",
        "low": "45",
        "text": "Sunny"
       },
       {
        "code": "34",
        "date": "4 Apr 2015",
        "day": "Sat",
        "high": "64",
        "low": "45",
        "text": "Mostly Sunny"
       },
       {
        "code": "11",
        "date": "5 Apr 2015",
        "day": "Sun",
        "high": "59",
        "low": "43",
        "text": "Showers"
       },
       {
        "code": "30",
        "date": "6 Apr 2015",
        "day": "Mon",
        "high": "62",
        "low": "48",
        "text": "Partly Cloudy"
       },
       {
        "code": "12",
        "date": "7 Apr 2015",
        "day": "Tue",
        "high": "56",
        "low": "44",
        "text": "Rain"
       }
      ],
      "guid": {
       "isPermaLink": "false",
       "content": "USCA1116_2015_04_07_7_00_PDT"
      }
     }
    },
    {
     "title": "Yahoo! Weather - San Jose, CA",
     "link": "http://us.rd.yahoo.com/dailynews/rss/weather/San_Jose__CA/*http://weather.yahoo.com/forecast/USCA0993_f.html",
     "description": "Yahoo! Weather for San Jose, CA",
     "language": "en-us",
     "lastBuildDate": "Fri, 03 Apr 2015 2:52 pm PDT",
     "ttl": "60",
     "location": {
      "city": "San Jose",
      "country": "United States",
      "region": "CA"
     },
     "units": {
      "distance": "mi",
      "pressure": "in",
      "speed": "mph",
      "temperature": "F"
     },
     "wind": {
      "chill": "72",
      "direction": "300",
      "speed": "13"
     },
     "atmosphere": {
      "humidity": "21",
      "pressure": "30.14",
      "rising": "2",
      "visibility": "10"
     },
     "astronomy": {
      "sunrise": "6:50 am",
      "sunset": "7:32 pm"
     },
     "image": {
      "title": "Yahoo! Weather",
      "width": "142",
      "height": "18",
      "link": "http://weather.yahoo.com",
      "url": "http://l.yimg.com/a/i/brand/purplelogo//uh/us/news-wea.gif"
     },
     "item": {
      "title": "Conditions for San Jose, CA at 2:52 pm PDT",
      "lat": "37.34",
      "long": "-121.89",
      "link": "http://us.rd.yahoo.com/dailynews/rss/weather/San_Jose__CA/*http://weather.yahoo.com/forecast/USCA0993_f.html",
      "pubDate": "Fri, 03 Apr 2015 2:52 pm PDT",
      "condition": {
       "code": "30",
       "date": "Fri, 03 Apr 2015 2:52 pm PDT",
       "temp": "72",
       "text": "Partly Cloudy"
      },
      "description": "\n<img src=\"http://l.yimg.com/a/i/us/we/52/30.gif\"/><br />\n<b>Current Conditions:</b><br />\nPartly Cloudy, 72 F<BR />\n<BR /><b>Forecast:</b><BR />\nFri - Sunny. High: 70 Low: 44<br />\nSat - Partly Cloudy. High: 66 Low: 44<br />\nSun - Showers. High: 61 Low: 43<br />\nMon - Partly Cloudy. High: 63 Low: 47<br />\nTue - Rain. High: 57 Low: 43<br />\n<br />\n<a href=\"http://us.rd.yahoo.com/dailynews/rss/weather/San_Jose__CA/*http://weather.yahoo.com/forecast/USCA0993_f.html\">Full Forecast at Yahoo! Weather</a><BR/><BR/>\n(provided by <a href=\"http://www.weather.com\" >The Weather Channel</a>)<br/>\n",
      "forecast": [
       {
        "code": "32",
        "date": "3 Apr 2015",
        "day": "Fri",
        "high": "70",
        "low": "44",
        "text": "Sunny"
       },
       {
        "code": "30",
        "date": "4 Apr 2015",
        "day": "Sat",
        "high": "66",
        "low": "44",
        "text": "Partly Cloudy"
       },
       {
        "code": "11",
        "date": "5 Apr 2015",
        "day": "Sun",
        "high": "61",
        "low": "43",
        "text": "Showers"
       },
       {
        "code": "30",
        "date": "6 Apr 2015",
        "day": "Mon",
        "high": "63",
        "low": "47",
        "text": "Partly Cloudy"
       },
       {
        "code": "12",
        "date": "7 Apr 2015",
        "day": "Tue",
        "high": "57",
        "low": "43",
        "text": "Rain"
       }
      ],
      "guid": {
       "isPermaLink": "false",
       "content": "USCA0993_2015_04_07_7_00_PDT"
      }
     }
    }
   ]
  }
 }
}
    """

  val weatherResponse = """{
      "query":{"count":1,"created":"2015-03-10T03:03:36Z","lang":"en-US",
      "results":
        {"channel":
          {"title":"Yahoo! Weather - Sunnyvale, CA",
          "link":"http://us.rd.yahoo.com/dailynews/rss/weather/Sunnyvale__CA/*http://weather.yahoo.com/forecast/USCA1116_f.html",
          "description":"Yahoo! Weather for Sunnyvale, CA",
          "language":"en-us",
          "lastBuildDate":"Mon, 09 Mar 2015 6:56 pm PDT",
          "ttl":"60",
          "location":{"city":"Sunnyvale","country":"United States","region":"CA"},
          "units":{"distance":"mi","pressure":"in","speed":"mph","temperature":"F"},
          "wind":{"chill":"61","direction":"350","speed":"7"},
          "atmosphere":{"humidity":"75","pressure":"29.93","rising":"0","visibility":"10"},
          "astronomy":{"sunrise":"7:26 am","sunset":"7:08 pm"},
          "image":{"title":"Yahoo! Weather","width":"142","height":"18","link":"http://weather.yahoo.com","url":"http://l.yimg.com/a/i/brand/purplelogo//uh/us/news-wea.gif"},
          "item":{"title":"Conditions for Sunnyvale, CA at 6:56 pm PDT","lat":"37.37","long":"-122.04","link":"http://us.rd.yahoo.com/dailynews/rss/weather/Sunnyvale__CA/*http://weather.yahoo.com/forecast/USCA1116_f.html","pubDate":"Mon, 09 Mar 2015 6:56 pm PDT",
          "condition":{"code":"34","date":"Mon, 09 Mar 2015 6:56 pm PDT","temp":"61","text":"Fair"},
          "description":"\n<img src=\"http://l.yimg.com/a/i/us/we/52/34.gif\"/><br />\n<b>Current Conditions:</b><br />\nFair, 61 F<BR />\n<BR /><b>Forecast:</b><BR />\nMon - Mostly Clear. High: 71 Low: 50<br />\nTue - Mostly Cloudy. High: 74 Low: 54<br />\nWed - AM Light Rain. High: 65 Low: 51<br />\nThu - Mostly Sunny. High: 71 Low: 51<br />\nFri - Partly Cloudy. High: 76 Low: 54<br />\n<br />\n<a href=\"http://us.rd.yahoo.com/dailynews/rss/weather/Sunnyvale__CA/*http://weather.yahoo.com/forecast/USCA1116_f.html\">Full Forecast at Yahoo! Weather</a><BR/><BR/>\n(provided by <a href=\"http://www.weather.com\" >The Weather Channel</a>)<br/>\n",
          "forecast":[{"code":"33","date":"9 Mar 2015","day":"Mon","high":"71","low":"50","text":"Mostly Clear"},
          {"code":"28","date":"10 Mar 2015","day":"Tue","high":"74","low":"54","text":"Mostly Cloudy"},
          {"code":"11","date":"11 Mar 2015","day":"Wed","high":"65","low":"51","text":"AM Light Rain"},
          {"code":"34","date":"12 Mar 2015","day":"Thu","high":"71","low":"51","text":"Mostly Sunny"},{"code":"30","date":"13 Mar 2015","day":"Fri","high":"76","low":"54","text":"Partly Cloudy"}],
          "guid":{"isPermaLink":"false","content":"USCA1116_2015_03_13_7_00_PDT"}}}}}}"""

  "Weather response" should {
    "contain date" in {
      val resp = Response(body=scalaz.stream.Process.eval(Task.now(ByteVector(weatherResponse.getBytes))))
      val x = JsonTransformer.asText(resp)
      x.run must contain ("1425952560000")
    }
  }

  "Weather response 2" should {
    "contain couple of dates" in {
      val resp = Response(body=scalaz.stream.Process.eval(Task.now(ByteVector(multiWeatherResponse.getBytes))))
      val x = JsonTransformer.asText(resp)
      x.run must contain ("1428097920000")
    }
  }
}