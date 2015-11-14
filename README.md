### Wookie - building data products

* Reuse components using Sparkles - data processing monads
* Map over generic lists of functions
* Use some Latitude/Longitude goodies
* Provides base classes for writing:
  - CLI applications
  - Spark / Spark Streaming jobs
  - REST APIs
* Provides collector API and sample collectors
* Spark SQL Server - automatically register tables given directory
* Data ingestion - REST API for pushing data to Kafka queue
* Prediction server - exposes Spark MLLib models as REST APIs

### Modules ##

* <b>app-api</b> - base classes/objects that helps writing basic commandline applications
* <b>web-api</b> - base classes/objects that helps writing REST APIs
* <b>spark-api</b> - utility classes/objects for writing Spark / Spark Streaming applications
* <b>spark-api-kafka</b> - utility classes/objects for writing Spark Streaming applications using Kafka input streams
* <b>spark-api-twitter</b> - utility classes/objects for writing Spark Streaming applications using Twitter input streams
* <b>oracle</b> - REST API server that predicts new data points based on the model (WIP)
* <b>pumper</b> - REST API server that push data to kafka queue (WIP)
* <b>sqlserver<b> - Spark SQL server that automatically register and refresh tables given root directory, supports local file formats like json, csv, parquet as well as remote ones like Cassandra or Elasticsearch
* <b>examples:</b>
  - <b>yql-app/analytics</b> - example analytics using twitter and kafka streams
  - <b>yql-app/collector</b> - example collectors using YQL https://developer.yahoo.com/yql/

### Dependencies

* Spire - https://github.com/non/spire
* Scalaz-Stream - https://github.com/scalaz/scalaz-stream]
* Shapeless - https://github.com/milessabin/shapeless
* Apache Spark - http://spark.apache.org
* Argonaut - http://argonaut.io
* Http4S - http://http4s.org
* Spark Connectors - Elasticsearch, Cassandra
