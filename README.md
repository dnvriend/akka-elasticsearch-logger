# akka-elasticsearch-logger
An ElasticSearch Logger for Akka. It uses logback-classic as the back-end.

# Usage

## Logger configuration
Configure the ElasticSearch index and type to use in application.conf:

    elasticsearch-logger {
      index  = "component-name"
      type = "logger"
    }

## Configure the appender 
The appender should be configured logback.xml

    <configuration>
        <appender name="es" class="com.github.dnvriend.elasticsearch.logger.ElasticSearchAppender">
            <encoder>
                <pattern>%date{HH:mm:ss} %-5level [%X{akkaSource}] - %msg%n</pattern>
            </encoder>
        </appender>
        <logger name="com.github.dnvriend" level="debug" additivity="false">
            <appender-ref ref="es"/>
        </logger>
        <logger name="akka.actor" level="debug" additivity="false">
            <appender-ref ref="es"/>
        </logger>
        <logger name="spray" level="debug" additivity="false">
            <appender-ref ref="es" />
        </logger>
        <root level="info">
            <appender-ref ref="es"/>
        </root>
    </configuration>


## Akka configuration
Akka should be configured to add the ElasticSearchLogger extension in application.conf:

    akka {
      log-dead-letters-during-shutdown = off
      log-dead-letters = off
      jvm-exit-on-fatal-error = off
    
      stdout-loglevel = info
      log-config-on-start = off
      loglevel = info
    
      loggers = ["akka.event.slf4j.Slf4jLogger"]
    
      logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
    
      extensions = ["com.github.dnvriend.elasticsearch.logger.ElasticSearchLogger"]
    }
