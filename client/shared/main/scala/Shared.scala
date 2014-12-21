package example

import scala.annotation.ClassfileAnnotation
import scala.concurrent._

case class User(name: String, id: Option[Int] = None)

trait Api{
  def list(path: String): Future[Seq[User]]
}
