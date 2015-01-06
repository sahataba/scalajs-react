package example

import scala.concurrent._

case class Date(utc:Long) extends AnyVal

case class User(name: String, id: Option[Int] = None, email:String, birthday:Date)

trait Api{
  def users(): Future[Seq[User]]
  def createUser(user:User):Future[User]
  def removeUser(id:Int):Future[Unit]
}
