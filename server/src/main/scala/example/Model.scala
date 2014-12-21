package example

import scala.slick.driver.H2Driver.simple._

import scala.slick.lifted.{ProvenShape, ForeignKeyQuery}
import scala.concurrent._
import scala.concurrent.duration._

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  // Auto Increment the id primary key column
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  // The name can't be null
  def name = column[String]("NAME", O.NotNull)
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a User
  def * = (name, id.?) <> (User.tupled, User.unapply)
}

// The main application
object TableModel {

  // The query interface for the Suppliers table
  val users: TableQuery[Users] = TableQuery[Users]

  lazy val db = Database.forURL("jdbc:h2:mem:hello;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  db.withSession{ implicit session =>
    users.ddl.create
    users += User("aurelius")
  }

  def list2:List[User] = db.withSession{ implicit session =>
    users.list
  }


}
