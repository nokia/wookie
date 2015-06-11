package wookie.yql.weather

import wookie.app.HttpToKafkaDumper
import wookie.yql.weather.WeatherDecoders.
_
object WeatherCollector extends HttpToKafkaDumper[List[Weather]]
