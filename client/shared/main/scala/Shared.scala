package example

import scala.concurrent._

import monocle.macros._
import monocle.syntax._
import monocle._

case class Date(utc:Long) extends AnyVal

sealed trait Role
case object Admin extends Role
case object Member extends Role

object Role {
  def parse(value:String):Role = value match {
    case "admin" => Admin
    case "member" => Member
  }
  def write(role:Role):String = role match {
    case Admin => "admin"
    case Member => "member"
  }
  val values = List(Admin, Member)
}


sealed trait IDLifecycle
case object Draft extends IDLifecycle
case object Saved extends IDLifecycle

trait EntityLifecycle {
  type E
  type ID
  //type LifeCycle <: IDLifecycle
  val entity:E
  val id:Option[ID]
  val lifecycle:IDLifecycle
}

case class DraftEntity[EN](
                            id:None.type,
                            entity:EN,
                            lifecycle:Draft.type) extends EntityLifecycle {
  type E = EN;
  type ID = None.type;
  type lifecycle = Draft.type
}
case class SavedEntity[EN](
                            id:Some[Int],
                            entity:EN,
                            lifecycle:Saved.type) extends EntityLifecycle {
  type E = EN;
  type ID = Int;
  type lifecycle = IDLifecycle
}
case class PublishedEntity[E](saved:SavedEntity[E], publishedDate:Int)

sealed trait User2[UserLifecycle] {

  def addId2[E](draft:DraftEntity[E]):SavedEntity[E] = SavedEntity(Some(5), draft.entity, Saved)

}

//case class Name[UserLifecycle](value:String) extends AnyVal

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
