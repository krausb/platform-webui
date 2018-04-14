package controllers

import akka.actor.ActorSystem
import javax.inject._
import play.api._
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's map view page.
  */
@Singleton
class MapViewController @Inject()(cc: ControllerComponents)(
    implicit actorSystem: ActorSystem,
    executionContext: ExecutionContext,
    webJarsUtil: org.webjars.play.WebJarsUtil)
    extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def mapView() = Action { implicit request: Request[AnyContent] =>
    val webSocketUrl = routes.DataStreamController.data().webSocketURL()
    Ok(views.html.mapview(webSocketUrl))
  }
}
