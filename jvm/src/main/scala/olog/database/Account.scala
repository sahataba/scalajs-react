package olog.database

import olog._
import slick.driver.H2Driver.api._

import scala.concurrent._
import Account.{Status, Role}

class Users(tag: Tag) extends TableWithId[Account.User](tag, "USERS") {

  import Columns._

  def firstName = column[String]("FIRST_NAME")
  def lastName = column[String]("LAST_NAME")
  def email = column[olog.Email]("EMAIL")
  def birthday = column[Date]("BIRTHDAY")
  def role = column[Role]("ROLE")
  def status = column[Status]("STATUS")


  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a User

  def toUser(firstName:String, lastName:String, id:Option[Int], email:Email, birthday:Date, role:Role, status:Status) = {
    Account.User(
      id = Some(Id(id.get)),
      firstName = firstName,
      lastName = lastName,
      email = email,
      birthday = birthday,
      role = role,
      status = status
    )
  }

  val toRecord: Account.User => Option[(String, String, Option[Int], Email, Date, Role, Status)] = user =>
    Some((
      user.firstName,
      user.lastName,
      user.id.map(_.value),
      user.email,
      user.birthday,
      user.role,
      user.status
    ))

  def * = (firstName, lastName, id.?, email, birthday, role, status) <> ((toUser _).tupled, toRecord)
}

// The main application
object AccountModel extends CRUD[Account.User, Users]{

  val profile = slick.driver.H2Driver

  // The query interface for the Suppliers table
  val table: TableQuery[Users] = TableQuery[Users]

  lazy val db = Database.forURL("jdbc:h2:mem:hello;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  val createActions = DBIO.seq(
    table.ddl.create,
    table += Account.User(
      firstName = "aurelius",
      lastName = "livingston",
      email = Email("great road"),
      birthday = olog.Date(1l),
      role = Account.Admin,
      status = Account.Approved)
  )

  db.run(createActions)

  def list2:Future[Seq[Account.User]] = db.run(table.result)

  val byIdActCompiled = Compiled((id:Rep[Int]) => table.filter(_.id === id))
  def byId(id:Int) = db.run(byIdActCompiled(id).result)

}


