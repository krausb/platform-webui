package controllers

import akka.actor.ActorSystem
import akka.event.Logging
import javax.inject._
import play.api._
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's debug log page.
  */
@Singleton
class DebugLogController @Inject()(cc: ControllerComponents)(
    implicit actorSystem: ActorSystem,
    executionContext: ExecutionContext,
    webJarsUtil: org.webjars.play.WebJarsUtil)
    extends AbstractController(cc) {

  private val logger = Logger(getClass)
  private implicit val logging =
    Logging(actorSystem.eventStream, logger.underlyingLogger.getName)

  /**
    * Debug Log View Action
    *
    * @return
    */
  def debugLog: Action[AnyContent] = Action { implicit request: RequestHeader =>
    val webSocketUrl = routes.DataStreamController.data().webSocketURL()
    logger.info(s"index: ")
    Ok(views.html.debuglog(webSocketUrl))
  }

}
