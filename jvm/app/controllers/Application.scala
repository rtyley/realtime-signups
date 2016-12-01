package controllers

import javax.inject.{Inject, _}

import akka.actor._
import play.api.mvc._

case class TweetInfo(whatever: String)


@Singleton
class Application @Inject() (implicit system: ActorSystem) extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def tester = Action {
    Ok(views.html.tester())
  }
}
