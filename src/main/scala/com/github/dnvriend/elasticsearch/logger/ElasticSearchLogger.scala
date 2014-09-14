package com.github.dnvriend.elasticsearch.logger

import akka.actor.Actor
import akka.event.Logging._
import com.dnvriend.elasticsearch.extension.ElasticSearch
import com.sksamuel.elastic4s.ElasticDsl._

import scala.concurrent.Future

object ElasticSearchLogger {

  import java.text.SimpleDateFormat
  import java.util.Date

  private val date = new Date()
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")

  def timestamp(event: LogEvent): String = synchronized {
    date.setTime(event.timestamp)
    dateFormat.format(date)
  } // SDF isn't threadsafe
}

class ElasticSearchLogger extends Actor {
  import ElasticSearchLogger._

  private val cfg = context.system.settings.config

  val indexName = cfg.getString("elasticsearch-logger.index")
  val typeName = cfg.getString("elasticsearch-logger.type")

  var es: Option[ElasticSearch] = None

  def print(event: Any): Unit = event match {
    case e: Error => error(e)
    case e: Warning => warning(e)
    case e: Info => info(e)
    case e: Debug => debug(e)
    case e => warning(Warning(simpleName(this), this.getClass, "received unexpected event of class " + e.getClass + ": " + e))
  }

  def log(logLevel: String, timestamp: String, threadName: String, logSource: String, message: String) = es match {
      case Some(s) =>
        s.doIndex {
          index into indexName -> typeName fields(
            "logLevel" -> logLevel,
            "timestamp" -> timestamp,
            "threadName" -> threadName,
            "logSource" -> logSource,
            "message" -> message
            )
        }
      case _ => println("No ElasticSearch, logging to STDOUT: " + message)
    }

  def error(event: Error): Unit = {
    log("ERROR",
      timestamp(event),
      event.thread.getName,
      event.logSource,
      s"[ERROR] [${timestamp(event)}] [${event.thread.getName}] [${event.logSource}] [${event.message}}]")
  }

  def warning(event: Warning): Unit =
    log("WARN",
      timestamp(event),
      event.thread.getName,
      event.logSource,
      s"[WARN] [${timestamp(event)}] [${event.thread.getName}] [${event.logSource}] [${event.message}}]")

  def info(event: Info): Unit =
    log("INFO",
      timestamp(event),
      event.thread.getName,
      event.logSource,
      s"[INFO] [${timestamp(event)}] [${event.thread.getName}] [${event.logSource}] [${event.message}}]")

  def debug(event: Debug): Unit =
    log("DEBUG",
      timestamp(event),
      event.thread.getName,
      event.logSource,
      s"[DEBUG] [${timestamp(event)}] [${event.thread.getName}] [${event.logSource}] [${event.message}}]")

  override def receive: Receive = {
    case InitializeLogger(_) =>
      println("ElasticSearch Logger Started")
      Future {
        ElasticSearch(context.system)
      }.map { es =>
        self ! Some(es)
      } recover {
        case t: Throwable => println(t.getMessage)
      }

      sender() ! LoggerInitialized

    case Some(m: ElasticSearch) =>
      println("ElasticSearch intialized, switching to ElasticSearch Logging")
      m.createIndex(indexName)
      this.es = Some(m)

    case event: LogEvent => print(event)
  }

  implicit val ec = context.system.dispatcher
}
