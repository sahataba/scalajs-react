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

  val db = Database.forURL("jdbc:h2:mem:hello", driver = "org.h2.Driver")


  def create():Unit = {};//db.withSession(implicit session => users.ddl.create)
  def list2:List[User] = List(User("jedan"), User("drugi"))//db.withSession(implicit session => users.list)

}
