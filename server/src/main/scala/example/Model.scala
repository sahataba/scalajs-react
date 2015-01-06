package example

import scala.slick.driver.H2Driver.api._

import scala.slick.lifted.{ProvenShape, ForeignKeyQuery}
import scala.concurrent._
import scala.concurrent.duration._

class Users(tag: Tag) extends Table[User](tag, "USERS") {

  // And a ColumnType that maps it to Int values 1 and 0
  implicit val boolColumnType = MappedColumnType.base[Date, Long](
  { b => b.utc},    // map Bool to Int
  { utc => Date(utc) } // map Int to Bool
  )

  // Auto Increment the id primary key column
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  // The name can't be null
  def name = column[String]("NAME", O.NotNull)
  def email = column[String]("EMAIL", O.NotNull)
  def birthday = column[Date]("BIRTHDAY", O.NotNull)

  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a User
  def * = (name, id.?, email, birthday) <> ((User.apply _).tupled, User.unapply)
}

// The main application
object TableModel {

  // The query interface for the Suppliers table
  val users: TableQuery[Users] = TableQuery[Users]

  lazy val db = Database.forURL("jdbc:h2:mem:hello;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  db.run(Action.seq(
    users.ddl.create,
    users += User(name = "aurelius", email = "great road", birthday = example.Date(1l))
  ))

  def list2:Future[Seq[User]] = db.run(users.result)
  def createUser(user:User):Future[Int] = db.run((users returning users.map(_.id)) += user)
  def removeUser(id:Option[Int]) = db.run(users.filter(_.id === id).delete)
}
