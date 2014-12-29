package example

import scala.annotation.ClassfileAnnotation
import scala.concurrent._

case class User(name: String, id: Option[Int] = None, email:String)

trait Api{
  def users(): Future[Seq[User]]
  def createUser(user:User):Future[User]
}
