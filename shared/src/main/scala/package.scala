package olog

import scala.concurrent._

import monocle.macros._
import monocle.syntax._
import monocle._

case class Date(utc:Long) extends AnyVal

trait Converter[E] {
  val map:Map[String, E]
  val values = map.values
  def read: String => Either[String, E] = value => map.get(value) match {
    case Some(entity) => Right(entity)
    case None => Left("missing key")
  }
  def write: E => String = map map (_.swap)

}

object StatusConverter extends Converter[Status[Account.User]]{
  val map = Map("Applied" -> Applied, "Approved" -> Approved)
}

object RoleConverter extends Converter[Role] {
  val map = Map("admin" -> Admin, "member" -> Member)
}

sealed trait Role
case object Admin extends Role
case object Member extends Role

sealed trait Status[E]
case object Applied extends Status[Account.User]
case object Approved extends Status[Account.User]

case class Id[E](value:Int)
case class Deleted[E](id:Id[E])
case class Created[E](value:E)

case class Email(email:String) extends AnyVal

object Email {
  val lenser = Lenser[Email]
  val _email = lenser(_.email)

  def parse(value:String):Either[String, Email] = {
    """\b[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*\b""".r.findFirstIn(value) match {
      case Some(_) => Right(Email(value))
      case None => Left("invalid email")
    }
  }
}

sealed trait Account
object Account {
  case class UserSession(id:String) extends Account
  case class User(
                   firstName: String,
                   lastName:String,
                   id: Option[Id[User]] = None,
                   email:Email,
                   birthday:Date,
                   role:Role,
                   status:Status[User]) extends Account

  object User {
    def dummy():User =  User(id = None, firstName = "", lastName = "", email = Email("dummy@gmail.com"), birthday = Date(1l), role = Admin, status = Applied)
    val lenser = Lenser[User]
    val _firstName = lenser(_.firstName)
    val _lastName = lenser(_.lastName)
    val _name:Getter[User,String] = Getter((user:User) => _firstName.get(user) + _lastName.get(user))
    val _email = lenser(_.email)
    val _birthday = lenser(_.birthday)
    val _role = lenser(_.role)
    val _id = lenser(_.id)
    val _status = lenser(_.status)
  }
}

case class Credentials(email:String, password:String)
object Credentials {
  val lenser = Lenser[Credentials]
  val _email = lenser(_.email)
  val _password = lenser(_.password)
}



trait Create[E] {
  def create(entity:E):Future[Created[E]]
}

trait Delete[E] {
  def delete(id:Id[E]):Future[Deleted[E]]
}

trait Query[E] {
  def all:Future[Seq[E]]
}

trait Api /*extends Create[User] with Delete[User]*/{

  def users(user:Account.UserSession): Future[Seq[Account.User]]
  def create(entity:Account.User):Future[Created[Account.User]]
  def delete(id:Id[Account.User]):Future[Deleted[Account.User]]
}

case class TodoItem(description:String)

trait TodoApi {
  def all():Future[Seq[TodoItem]]
  def create(item:TodoItem):Future[TodoItem]
}
