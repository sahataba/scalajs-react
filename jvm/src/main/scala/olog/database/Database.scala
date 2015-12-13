package olog.database

import olog._
import scala.concurrent.{Promise, Future}
import scalaz.concurrent.{Task}
import doobie.imports._

object db {

  implicit final class TaskExtensionOps[A](x: => Task[A]) {
    import scalaz.{ \/-, -\/ }
    val p: Promise[A] = Promise()
    def runFuture(): Future[A] = {
      x.runAsync {
        case -\/(ex) =>
          p.failure(ex); ()
        case \/-(r) => p.success(r); ()
      }
      p.future
    }
  }

  val xa = DriverManagerTransactor[Task]("org.postgresql.Driver", "jdbc:postgresql:todos", "postgres", "")

  implicit val RoleMeta: Meta[Account.Role] =
    Meta[String].
      nxmap(
        s => Account.Role.read(s).right.get,
        role => {println(s"FFFFFF $role");Account.Role.write(role)}
      )

  implicit val StatusMeta: Meta[Account.Status] =
    Meta[String].
      nxmap(
        s => Account.Status.read(s).right.get,
        status => Account.Status.write(status)
      )

  implicit val AccountIdMeta: Meta[Account.Id] =
    Meta[Int].
      xmap(
        s => new Account.Id(s),
        id => id.value
      )

  implicit val DateMeta: Meta[Date] =
    Meta[Long].
      xmap(
        s => Date(s),
        (date:Date) => date.utc
      )

  /*

   def create(entity:E):Future[Created[Id[E]]] =
    (db.run((table returning table.map(_.id)) += entity)).
      map(a => Created(Id(a)))

       val findById = table.findBy(_.id)

  def delete(id:Id[E]):Future[Deleted[E]] = {
    val act =
      findById(id.value).
        delete.
        map(_ => Deleted(id))
    db.run(act)
  }

  def fetchThenUpdate(id:Id[E], upd: E => E):Future[E] =  {
    val act = for {
      ent <- findById(id.value).result.head
      updated = upd(ent)
      _ <- {println(s"TTT: $updated");findById(id.value).update(updated)}
    } yield updated
    db.run(act)
  }
   */


  def fetchThenUpdate(id:Account.Id, upd: Account.Record => Account.Record):Future[Account.Record] =  ???

  def create(entity:Account.Record):Future[Created[Account.Id]] = ???

  def delete(id:Account.Id):Future[Deleted[Account.Record]] = ???

  def list:Future[List[Account.Record]] =
    sql"select first_name, last_name, id, email, birthday, role, status  from users"
      .query[Account.Record] // Query0[String]
      .list          // ConnectionIO[List[String]]
      .transact(xa)  // Task[List[String]]
      .runFuture

  def byId(id:Int):Future[Option[Account.Record]] = ???

  def all:Future[List[Todo.Item]] =
    sql"select description  from todos"
      .query[Todo.Item] // Query0[String]
      .list          // ConnectionIO[List[String]]
      .transact(xa)  // Task[List[String]]
      .runFuture

  def create(description:String):Future[Int] =
    sql"insert into todos (description) values ($description)"
      .update
      .run
      .transact(xa)
      .runFuture


}


