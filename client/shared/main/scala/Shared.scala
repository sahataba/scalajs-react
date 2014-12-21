package example

import scala.annotation.ClassfileAnnotation
case class User(name: String, id: Option[Int] = None)

trait Api{
  def list(path: String): Seq[User]
}
