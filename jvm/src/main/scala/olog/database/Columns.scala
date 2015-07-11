package olog.database

import slick.driver.H2Driver.api._
import olog._
import Account.{Role,Status}

object Columns {
  implicit val idColumnType = MappedColumnType.base[Id[Account.User], Int](
    b => b.value,
    id => Id(id)
  )

  // And a ColumnType that maps it to Int values 1 and 0
  implicit val boolColumnType = MappedColumnType.base[Date, Long](
  { b => b.utc},    // map Bool to Int
  { utc => Date(utc) } // map Int to Bool
  )

  implicit val boolColumnType2 = MappedColumnType.base[Role, String](
    Role.write, (str:String) => Role.read(str).right.get
  )

  implicit val statusColumnType2 = MappedColumnType.base[Status, String](
    Status.write, (str:String) => Status.read(str).right.get
  )

  implicit val emailColumnType2 = MappedColumnType.base[olog.Email, String](
    (email:Email) => email.email, olog.Email apply _
  )
}