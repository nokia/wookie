package wookie.yql.weather

import wookie.collector.cli.HttpToKafkaDumper
import wookie.yql.weather.WeatherDecoders.
_
object WeatherCollector extends HttpToKafkaDumper[List[Weather]]
