package com.github.dnvriend.elasticsearch.logger

import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date

import akka.actor._
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.encoder.Encoder
import ch.qos.logback.core.status.ErrorStatus
import com.github.dnvriend.elasticsearch.extension.ElasticSearch
import com.sksamuel.elastic4s.ElasticDsl

object ElasticSearchLogger extends ExtensionId[ElasticSearchLogger] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): ElasticSearchLogger = new ElasticSearchLogger(system)

  override def lookup(): ExtensionId[_ <: Extension] = ElasticSearchLogger
}

class ElasticSearchLogger(system: ExtendedActorSystem) extends Extension {
  GlobalActorSystem.system = Some(system)
}

object GlobalActorSystem {
  var system: Option[ActorSystem] = None
}

class ElasticSearchAppender extends AppenderBase[ILoggingEvent] {
  import ElasticDsl._
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")

  var initialized = false
  var encoder: Option[Encoder[ILoggingEvent]] = None

  def setEncoder(encoder: Encoder[ILoggingEvent]): Unit = {
    this.encoder = Some(encoder)
  }

  override def start(): Unit = {
    encoder match {
      case Some(enc) =>
        super.start()
      case None =>
        addStatus(new ErrorStatus(s"No encoder set for the appender named $name.", this))
    }
  }

  def log(logLevel: String, timestamp: String, threadName: String, logSource: String, message: String) = GlobalActorSystem.system match {
    case Some(system) =>
      val cfg = system.settings.config
      val indexName = cfg.getString("elasticsearch-logger.index")
      val typeName = cfg.getString("elasticsearch-logger.type")
      if(!initialized) {
        ElasticSearch(system).createIndex(indexName)
        initialized = true
      }
      ElasticSearch(system).doIndex {
        index into indexName -> typeName fields(
          "logLevel" -> logLevel,
          "timestamp" -> timestamp,
          "threadName" -> threadName,
          "logSource" -> logSource,
          "message" -> message
          )
      }
    case None =>
  }

  override def append(event: ILoggingEvent): Unit = {
    val formattedString = encoder.map { enc =>
      val bos = new ByteArrayOutputStream()
      enc.init(bos)
      enc.doEncode(event)
      enc.close()
      new String(bos.toByteArray)
    }
    log(event.getLevel.toString, dateFormat.format(new Date), event.getThreadName, event.getLoggerName, formattedString.getOrElse(event.getMessage))
  }
}