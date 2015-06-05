package example

import scala.concurrent._

import monocle.macros._
import monocle.syntax._
import monocle._

case class Date(utc:Long) extends AnyVal

trait Converter[E] {
  val values:Map[String, E]
  def read: String => Either[String, E] = value => values.get(value) match {
    case Some(entity) => Right(entity)
    case None => Left("missing key")
  }
  def write: E => String = values map (_.swap)

}

object StatusConverter extends Converter[Status[User]]{
  val values = Map("Applied" -> Applied, "Approved" -> Approved)
  val statuses = List(Applied, Approved)
}

object RoleConverter extends Converter[Role] {
  val values = Map("admin" -> Admin, "member" -> Member)
  val roles:List[Role] = List(Admin, Member)
}

sealed trait Role
case object Admin extends Role
case object Member extends Role

sealed trait Status[E]
case object Applied extends Status[User]
case object Approved extends Status[User]

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

case class UserSession(id:String)

case class User(
                 firstName: String,
                 lastName:String,
                 id: Option[Id[User]] = None,
                 email:Email,
                 birthday:Date,
                 role:Role,
                 status:Status[User])

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

trait Create[E] {
  def create(entity:E):Future[Created[E]]
}

trait Delete[E] {
  def delete(id:Id[E]):Future[Deleted[E]]
}

trait Api extends Create[User] with Delete[User]{

  def users(user:UserSession): Future[Seq[User]]
  def create(entity:User):Future[Created[User]]
  def delete(id:Id[User]):Future[Deleted[User]]
}
