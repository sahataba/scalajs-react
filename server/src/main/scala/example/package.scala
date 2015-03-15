package example

import slick.driver.H2Driver.api._

import slick.lifted.{ProvenShape, ForeignKeyQuery}
import scala.concurrent._
import slick.driver.JdbcProfile
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

abstract class TableWithId[A](tag:Tag, name:String) extends Table[A](tag:Tag, name) {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
}

class Users(tag: Tag) extends TableWithId[User](tag, "USERS") {

  implicit val idColumnType = MappedColumnType.base[Id[User], Int](
   b => b.value,
   id => Id(id)
  )

  // And a ColumnType that maps it to Int values 1 and 0
  implicit val boolColumnType = MappedColumnType.base[Date, Long](
  { b => b.utc},    // map Bool to Int
  { utc => Date(utc) } // map Int to Bool
  )

  implicit val boolColumnType2 = MappedColumnType.base[example.Role, String](
    RoleConverter.write, (str:String) => RoleConverter.read(str).right.get
  )

  implicit val statusColumnType2 = MappedColumnType.base[example.Status[User], String](
    StatusConverter.write, (str:String) => StatusConverter.read(str).right.get
  )

  implicit val emailColumnType2 = MappedColumnType.base[example.Email, String](
    (email:Email) => email.email, example.Email apply _
  )

  def firstName = column[String]("FIRST_NAME")
  def lastName = column[String]("LAST_NAME")
  def email = column[example.Email]("EMAIL")
  def birthday = column[Date]("BIRTHDAY")
  def role = column[example.Role]("ROLE")
  def status = column[example.Status[User]]("STATUS")


  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a User

  def toUser(firstName:String, lastName:String, id:Option[Int], email:Email, birthday:Date, role:Role, status:Status[User]) = {
    User(
      id = Some(Id(id.get)),
      firstName = firstName,
      lastName = lastName,
      email = email,
      birthday = birthday,
      role = role,
      status = status
    )
  }

  val toRecord: User => Option[(String, String, Option[Int], Email, Date, Role, Status[User])] = user =>
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
object TableModel extends CRUD[User, Users]{

  val profile = slick.driver.H2Driver

  // The query interface for the Suppliers table
  val table: TableQuery[Users] = TableQuery[Users]

  lazy val db = Database.forURL("jdbc:h2:mem:hello;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  val createActions = DBIO.seq(
    table.ddl.create,
    table += User(firstName = "aurelius", lastName = "livingston", email = Email("great road"), birthday = example.Date(1l), role = Admin, status = Approved)
  )

  db.run(createActions)

  def list2:Future[Seq[User]] = db.run(table.result)

  val byIdActCompiled = Compiled((id:Rep[Int]) => table.filter(_.id === id))
  def byId(id:Int) = db.run(byIdActCompiled(id).result)

}

trait CRUD[E,T <: TableWithId[E]] extends CreateDBAction[E, T]{
  val profile:JdbcProfile
  val db:Database
  val table: TableQuery[T]


  //val del2 = (id:Rep[Int]) => table.filter(_.id === Id(id)).delete

  def delete(id:Id[E]):Future[Deleted[E]] = {
    db.run(table.filter(_.id === id.value).delete.map(_ => Deleted(id)))
  }

  val findById = table.findBy(_.id)

  def fetchThenUpdate(id:Id[E], upd: E => E):Future[E] =  {
    val act = for {
      ent <- findById(id.value).result.head
      updated = upd(ent)
      _ <- {println(s"TTT: $updated");findById(id.value).update(updated)}
    } yield updated
    db.run(act)
  }
}


trait CreateDBAction[E,T <: TableWithId[E]] {

  val profile:JdbcProfile
  val db:Database
  val table: TableQuery[T]

  def create(entity:E):Future[Created[E]] = (db.run((table returning table.map(_.id)) += entity)).map(a => Created(Id[E](a)))
}
