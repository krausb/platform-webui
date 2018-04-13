package controllers

import java.net.URI

import akka.actor.ActorSystem
import akka.event.Logging
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub}
import com.typesafe.config.ConfigFactory
import io.streamarchitect.platform.domain.codec.DomainCodec
import io.streamarchitect.platform.domain.telemetry.PositionedTelemetry
import javax.inject._
import org.apache.kafka.common.serialization.Deserializer
import play.api._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * DataStreamController creates a WebSocket as a response
  * which consumes data from a configured Kafka topic and broadcasts it to all clients
  */
@Singleton
class DataStreamController @Inject()(cc: ControllerComponents)(
    implicit actorSystem: ActorSystem,
    mat: Materializer,
    executionContext: ExecutionContext,
    webJarsUtil: org.webjars.play.WebJarsUtil)
    extends AbstractController(cc)
    with RequestMarkerContext {

  private type WSMessage = String

  private val config = ConfigFactory.load()
  private val log = Logger(getClass)
  private implicit val logging =
    Logging(actorSystem.eventStream, log.underlyingLogger.getName)

  log.info("Creating MergeHub source..")
  val source = MergeHub
    .source[WSMessage]
    .log("source", msg => log.info(s"WSMessage arrived: ${msg}"))

  val consumerSettings = ConsumerSettings(
    actorSystem,
    Option.empty[Deserializer[String]],
    Option.empty[Deserializer[Array[Byte]]]
  )
  log.info(s"Kafka Consumer Settings: ${consumerSettings}")

  log.info(s"Creating BroadcastHub sink..")
  val sink = BroadcastHub.sink[WSMessage]

  val dataSource =
    Consumer
      .committableSource(
        consumerSettings,
        Subscriptions.topics(config.getString("dataSource.topic")))
      .map(
        cmsg => {
          log.info(s"Incoming msg: ${cmsg}")
          DomainCodec
            .decode[PositionedTelemetry](
              cmsg.record.value(),
              PositionedTelemetry.SCHEMA$
            )
            .toString
        }
      )

  log.info(s"Merging websocket source with kafka source..")

  private val dataFlow: Flow[WSMessage, WSMessage, _] = {
    log.error("Joining sink with source...")
    Flow[WSMessage]
      .via(Flow.fromSinkAndSource(sink, dataSource))
      .log("dataFlow")
  }

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def data(): WebSocket = {
    WebSocket.acceptOrResult[WSMessage, WSMessage] {
      case rh if checkWSOrigin(rh) =>
        Future
          .successful(dataFlow)
          .map { flow =>
            log.info(s"Returning new data flow for ${rh}")
            Right(flow)
          }
          .recover {
            case e: Exception =>
              val msg = "Cannot create websocket"
              log.error(msg, e)
              val result = InternalServerError(msg)
              Left(result)
          }

      case rejected =>
        log.error(s"Request ${rejected} failed same origin check")
        Future.successful {
          Left(Forbidden("forbidden"))
        }
    }
  }

  /**
    * This is necessary to protect against Cross-Site WebSocket Hijacking as
    * WebSocket does not implement Same Origin Policy.
    *
    * See
    * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
    * and
    * https://tools.ietf.org/html/rfc6455#section-1.3
    */
  private def checkWSOrigin(implicit rh: RequestHeader): Boolean = {
    // The Origin header is the domain the request originates from.
    // https://tools.ietf.org/html/rfc6454#section-7
    log.debug("Checking the ORIGIN ")

    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
        log.debug(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        log.error(
          s"originCheck: rejecting request because Origin header value ${badOrigin} is not in the same origin")
        false

      case None =>
        log.error(
          "originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
    * Returns true if the value of the Origin header contains an acceptable value.
    */
  private def originMatches(origin: String): Boolean = {
    try {
      val url = new URI(origin)
      url.getHost == "localhost" &&
      (url.getPort match { case 9000 | 19001 => true; case _ => false })
    } catch {
      case e: Exception => false
    }
  }

}
