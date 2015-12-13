package olog

import scala.language.higherKinds
import autowire._
import japgolly.scalajs.react._
import monocle.macros.Lenser
import olog.User.{Credentials, Session}
import monocle.macros._
import monocle.std.option.some
import org.scalajs.dom.{console}
import scala.concurrent.ExecutionContext.Implicits.global


object State {
  final case class UserS(user: Option[Session], credentials:Option[Credentials])
  object UserS {
    val lenser = GenLens[UserS]
    val _credentials = lenser(_.credentials)
    val _email = _credentials composePrism some composeLens Credentials._email
    val _password = _credentials composePrism some composeLens Credentials._password
    val _user = lenser(_.user)

    class Backend($: BackendScope[Unit, UserS]) {

      def onChangeEmail(e: ReactEventI) = {
        $.modState(UserS._email.set(e.target.value))
      }

      def onChangePassword(e: ReactEventI) = {
        $.modState(UserS._password.set(e.target.value))
      }

      def handleSubmit(e: ReactEventI) = {
        e.preventDefault()
        val credentials = $.get().credentials
        credentials match {
          case Some(c) => Client[olog.Api].login(c).call().map(i => $.modState(UserS._user.set(i)))
          case None => console.log("bb")
        }
      }
    }
  }

}