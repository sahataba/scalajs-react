package example

import scala.slick.driver.H2Driver.api._

import scala.slick.lifted.{ProvenShape, ForeignKeyQuery}
import scala.concurrent._
import scala.slick.driver.JdbcProfile
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

abstract class TableWithId[A](tag:Tag, name:String) extends Table[A](tag:Tag, name) {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
}

class Users(tag: Tag) extends TableWithId[User](tag, "USERS") {

  // And a ColumnType that maps it to Int values 1 and 0
  implicit val boolColumnType = MappedColumnType.base[Date, Long](
  { b => b.utc},    // map Bool to Int
  { utc => Date(utc) } // map Int to Bool
  )

  implicit val boolColumnType2 = MappedColumnType.base[example.Role, String](
  example.Role.write, (str:String) => example.Role.parse(str).right.get
  )

  implicit val statusColumnType2 = MappedColumnType.base[example.Status, String](
    example.Status.write, (str:String) => example.Status.parse(str).right.get
  )

  implicit val emailColumnType2 = MappedColumnType.base[example.Email, String](
    (email:Email) => email.email, example.Email apply _
  )

  def firstName = column[String]("FIRST_NAME", O.NotNull)
  def lastName = column[String]("LAST_NAME", O.NotNull)
  def email = column[example.Email]("EMAIL", O.NotNull)
  def birthday = column[Date]("BIRTHDAY", O.NotNull)
  def role = column[example.Role]("ROLE", O.NotNull)
  def status = column[example.Status]("STATUS", O.NotNull)


  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a User
  def * = (firstName, lastName, id.?, email, birthday, role, status) <> ((User.apply _).tupled, User.unapply)
}

// The main application
object TableModel extends CRUD[User, Users]{

  val profile = scala.slick.driver.H2Driver

  // The query interface for the Suppliers table
  val table: TableQuery[Users] = TableQuery[Users]

  lazy val db = Database.forURL("jdbc:h2:mem:hello;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  db.run(Action.seq(
    table.ddl.create,
    table += User(firstName = "aurelius", lastName = "livingston", email = Email("great road"), birthday = example.Date(1l), role = Admin, status = Pending)
  ))

  def list2:Future[Seq[User]] = db.run(table.result)

}

trait CRUD[E,T <: TableWithId[E]] {
  val profile:JdbcProfile
  val db:Database
  val table: TableQuery[T]

  type Id = Option[Int]

  def create(entity:E):Future[Int] = db.run((table returning table.map(_.id)) += entity)

  def remove(id:Id):Future[Int] = db.run(table.filter(_.id === id).delete)

  def update(id:Id, upd: E => E):Future[E] =  {
    val act = for {
      ent <- table.filter(_.id === id).result.head
      updated = upd(ent)
      _ <- table.filter(_.id ===id).update(updated)
    } yield updated
    db.run(act)
  }
}
