package example

import scala.slick.driver.H2Driver.api._

import scala.slick.lifted.{ProvenShape, ForeignKeyQuery}
import scala.concurrent._
import scala.concurrent.duration._

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  // Auto Increment the id primary key column
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  // The name can't be null
  def name = column[String]("NAME", O.NotNull)
  def email = column[String]("EMAIL", O.NotNull)

  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a User
  def * = (name, id.?, email) <> (User.tupled, User.unapply)
}

// The main application
object TableModel {

  // The query interface for the Suppliers table
  val users: TableQuery[Users] = TableQuery[Users]

  lazy val db = Database.forURL("jdbc:h2:mem:hello;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  db.run(Action.seq(
    users.ddl.create,
    users += User(name = "aurelius", email = "great road")
  ))

  def list2:Future[Seq[User]] = db.run(users.result)
  def createUser(user:User) = db.run(users += user)
}
