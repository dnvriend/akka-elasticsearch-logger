# akka-elasticsearch-logger
An ElasticSearch Logger for Akka

# Usage

## Logger configuration
Configure the logger in application.conf. You should configure the index name and type that will be used
to write the log messages to.  

```
elasticsearch-logger {
  index  = "component-name"
  type = "logger"
}
```

## Akka configuration
Akka should be configured to add an event handler in application.conf:

```
akka {
   event-handlers = ["com.github.dnvriend.elasticsearch.logger.ElasticSearchLogger"]
   loglevel = "info"
}
```

For obvious reasons, please don't set the loglevel lower than INFO, because this will fill up your
elasticsearch, in 10 seconds I got 80000 log lines, that's a whole lot-a-logs right there!

# Todo:
* Creating a slf4j-api version so logging can be filtered, and debug is an option again
