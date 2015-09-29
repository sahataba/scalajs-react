package olog

import scala.language.higherKinds
import scala.concurrent._

import shapeless.tag.@@
import monocle.macros._
import monocle._

import eu.timepit.refined._
import eu.timepit.refined.implicits._
import eu.timepit.refined.string._
import eu.timepit.refined.char._
import eu.timepit.refined.boolean._
import eu.timepit.refined.collection._


final case class Date(utc:Long) extends AnyVal

trait Converter[E] {
  val map:Map[String, E]
  val values = map.values
  def read: String => Either[String, E] = value => map.get(value) match {
    case Some(entity) => Right(entity)
    case None => Left("missing key")
  }
  def write: E => String = map map (_.swap)

}

final case class Id[E](value:Int)
final case class Deleted[E](id:Id[E])
final case class Created[E](value:E)

final case class Email(email:String) extends AnyVal

object Email {
  val lenser = GenLens[Email]
  val _email = lenser(_.email)

  def parse(value:String):Either[String, Email] = {
    """\b[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*\b""".r.findFirstIn(value) match {
      case Some(_) => Right(Email(value))
      case None => Left("invalid email")
    }
  }
}

package Account {

  sealed trait Account

  final case class Session(id:Id[User]) extends Account

  final case class Info(id:Id[User],role:Role) extends Account
  object Info {
    val lenser = GenLens[Info]
    val _role = lenser(_.role)
    val _id = lenser(_.id)
    def from(user:User):Info = Info(id = user.id.get, role = user.role)
  }


  final case class User(
                   firstName: String,
                   lastName:String,
                   id: Option[Id[User]] = None,
                   email:Email,
                   birthday:Date,
                   role:Role,
                   status:Status) extends Account

  object User {
    def dummy():User =  User(id = None, firstName = "", lastName = "", email = Email("dummy@gmail.com"), birthday = Date(1l), role = Admin, status = Account.Applied)
    val lenser = GenLens[User]
    val _firstName = lenser(_.firstName)
    val _lastName = lenser(_.lastName)
    val _name:Getter[User,String] = Getter((user:User) => _firstName.get(user) + _lastName.get(user))
    val _email = lenser(_.email)
    val _birthday = lenser(_.birthday)
    val _role = lenser(_.role)
    val _id = lenser(_.id)
    val _status = lenser(_.status)
  }
  final case class Credentials(email:String, password:String)
  object Credentials {
    val lenser = GenLens[Credentials]
    val (_email, _password) = (lenser(_.email), lenser(_.password))
  }

  sealed class Status
  object Status extends Enum[Status, String] with Converter[Account.Status]{
    val map:Map[String,Status] = Map("applied" -> Applied, "approved" -> Approved)
  }
  case object Applied extends Status
  case object Approved extends Status

  sealed class Role
  object Role extends Enum[Role, String] with Converter[Account.Role]{
    val map:Map[String, Role] = Map("admin" -> Admin, "member" -> Member)
  }
  case object Admin extends Role
  case object Member extends Role

}

trait Enum[E,D] {
  val map:Map[D,E]
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

object Amazon {
  val u1: String @@ LowerCaseUrl = "http://example.com"
  type LowerCaseUrl = Url And Forall[Not[UpperCase]]
  type Email2 = MatchesRegex[W.`"^[A-Za-z0-9+_.-]+@(.+)$"`.T]
  val validEmail:String @@ Email2 = "rudi@gmailcom"
}

trait Api /*extends Create[User] with Delete[User]*/{

  def users(user:Account.Session): Future[Seq[Account.Info]]
  def create(entity:Account.User):Future[Created[Account.User]]
  def delete(id:Id[Account.User]):Future[Deleted[Account.User]]
  def login(credentials:Account.Credentials):Future[Option[Account.Session]]
}

object Todo {
  sealed trait Todo
  final case class Item(description:String) extends Todo
}

trait TodoApi {
  def all():Future[Seq[Todo.Item]]
  def create(item:Todo.Item):Future[Todo.Item]
}
