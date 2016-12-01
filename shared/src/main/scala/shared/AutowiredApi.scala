package shared

import scala.concurrent.Future

// shared interface
trait AutowiredApi {

  def makeAHit(): Future[String]
}
