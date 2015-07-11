package olog.database

import slick.driver.H2Driver.api._

import slick.lifted.{ProvenShape, ForeignKeyQuery}
import scala.concurrent._
import slick.driver.JdbcProfile
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import olog._

abstract class TableWithId[A](tag:Tag, name:String) extends Table[A](tag:Tag, name) {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
}

trait CRUD[E,T <: TableWithId[E]] extends CreateDBAction[E, T]{
  val profile:JdbcProfile
  val db:Database
  val table: TableQuery[T]


  //val del2 = (id:Rep[Int]) => table.filter(_.id === Id(id)).delete
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
}


trait CreateDBAction[E,T <: TableWithId[E]] {

  val profile:JdbcProfile
  val db:Database
  val table: TableQuery[T]

  def create(entity:E):Future[Created[Id[E]]] =
    (db.run((table returning table.map(_.id)) += entity)).
      map(a => Created(Id(a)))
}