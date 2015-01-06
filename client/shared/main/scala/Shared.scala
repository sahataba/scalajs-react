package example

import scala.concurrent._

import monocle.macros.{Lenser, Lenses}
import monocle.syntax._

object UserLenses {
  val lenser = Lenser[User]
  val name = lenser(_.name)
  val email = lenser(_.email)
}

case class Date(utc:Long) extends AnyVal

sealed trait Role
case object Admin extends Role

case class User(name: String, id: Option[Int] = None, email:String, birthday:Date, role:Role)

object User {
  def dummy():User =  User(id = None, name = "", email = "", birthday = Date(1l), role = Admin)
}

trait Api{
  def users(): Future[Seq[User]]
  def createUser(user:User):Future[User]
  def removeUser(id:Int):Future[Unit]
}
