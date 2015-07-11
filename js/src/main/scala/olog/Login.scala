package olog

import autowire._
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom.{console}

import japgolly.scalajs.react._, vdom.prefix_<^._
import monocle.macros._
import monocle.syntax._
import monocle._
import monocle.std.option.some
import scalaz.StateT
import scalaz.effect.IO
import ScalazReact._

object LoginPage {

  import Account.{Session, Credentials}

  case class State(user: Option[Session], credentials:Option[Credentials])
  object State {
    val lenser = Lenser[State]
    val _credentials = lenser(_.credentials)
    val _email = _credentials composePrism some composeLens Credentials._email
    val _password = _credentials composePrism some composeLens Credentials._password
    val _user = lenser(_.user)
  }

  class Backend($: BackendScope[Unit, State]) {

    def onChangeEmail(e: ReactEventI) = {
      $.modState(State._email.set(e.target.value))
    }

    def onChangePassword(e: ReactEventI) = {
      $.modState(State._password.set(e.target.value))
    }

    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      val credentials = $.get().credentials
      credentials match {
        case Some(c) => Client[olog.Api].login(c).call().map(i => $.modState(State._user.set(i)))
        case None => console.log("bb")
      }
    }
  }

  val component = ReactComponentB[Unit]("Login")
    .initialState(State(None, Some(Credentials("",""))))
    .backend(new Backend(_))
    .render((P, S, B) =>
      <.div(
        <.form(
          ^.cls := "col s12",
          <.div(
            ^.cls := "row",
            <.div(
              ^.cls := "row",
              <.div(
                ^.cls := "input-field col s12",
                <.input(^.id := "password", ^.`type` := "password", ^.cls := "validate",  ^.onChange ==> B.onChangePassword, ^.value := S.credentials.map(_.password)),
                <.label(^.`for` := "password", "Password")
              )
            ),
            <.div(
              ^.cls := "row",
              <.div(
                ^.cls := "input-field col s12",
                <.input(^.id := "email", ^.`type` := "email", ^.cls := "validate", ^.onChange ==> B.onChangeEmail, ^.value := S.credentials.map(_.email)),
                <.label(^.`for` := "email", "Email")
              )
            ),
            <.div(
              ^.cls := "row",
              <.div(
                <.a(^.id := "login", ^.`type` := "button", ^.cls := "waves-light btn", ^.onClick ==> B.handleSubmit)("Login")
              )
            )
          )
        )
      )
  ).buildU

  def apply() = component()

}
