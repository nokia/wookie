### Wookie - building data products

* Reuse components using Sparkles - data processing monads
* Map over generic lists of functions
* Provides base classes for writing:
  - CLI applications
  - Spark / Spark Streaming jobs
* Provides collector API and sample collectors
* Spark SQL Server - automatically register tables given directory (supports json, parquet, csv, jdbc and cassandra)

### Modules ##

* <b>app-api</b> - base classes/objects that helps writing basic commandline applications
* <b>collector-api</b> - base classes to write collectors
* <b>spark-api</b> - utility classes/objects for writing Spark / Spark Streaming applications
* <b>spark-api-kafka</b> - utility classes/objects for writing Spark Streaming applications using Kafka input streams
* <b>sqlserver</b> - Spark SQL server that automatically register and refresh tables given root directory, supports local file formats like json, csv, parquet as well as remote ones like Cassandra or Elasticsearch
* <b>examples/</b>
  - <b>yql-collector</b> - example collectors using YQL https://developer.yahoo.com/yql

### Dependencies

* Algebird - https://github.com/twitter/algebird
* Scalaz-Stream - https://github.com/scalaz/scalaz-stream
* Shapeless - https://github.com/milessabin/shapeless
* Apache Spark - http://spark.apache.org
* Argonaut - http://argonaut.io
* Http4S - http://http4s.org
* Spark Connectors - [Cassandra](https://github.com/datastax/spark-cassandra-connector)
