package example

import scala.concurrent._

import monocle.macros._
import monocle.syntax._
import monocle._

case class Date(utc:Long) extends AnyVal

sealed trait Role
case object Admin extends Role

case class User(
                 firstName: String,
                 lastName:String,
                 id: Option[Int] = None,
                 email:String,
                 birthday:Date,
                 role:Role)

object User {
  def dummy():User =  User(id = None, firstName = "", lastName = "", email = "", birthday = Date(1l), role = Admin)
  val lenser = Lenser[User]
  val _firstName = lenser(_.firstName)
  val _lastName = lenser(_.lastName)
  val _name:Getter[User,String] = Getter((user:User) => _firstName.get(user) + _lastName.get(user))
  val _email = lenser(_.email)
  val _birthday = lenser(_.birthday)
  val _role = lenser(_.role)
  val _id = lenser(_.id)
}

trait Api{
  def users(): Future[Seq[User]]
  def createUser(user:User):Future[User]
  def removeUser(id:Int):Future[Unit]
}
