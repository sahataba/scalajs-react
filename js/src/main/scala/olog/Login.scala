package olog

import japgolly.scalajs.react._, vdom.prefix_<^._
import monocle.macros._
import monocle.syntax._
import monocle._
import monocle.std.option.some

object LoginPage {

  case class State(user: Option[UserSession], credentials:Option[Credentials])
  object State {
    val lenser = Lenser[State]
    val _credentials = lenser(_.credentials)
    val _email = _credentials composePrism some composeLens Credentials._email
    val _password = _credentials composePrism some composeLens Credentials._password
  }

  class Backend($: BackendScope[Unit, State]) {
    def onChangeEmail(e: ReactEventI) =
      $.modState(State._email.set(e.target.value))

    def onChangePassword(e: ReactEventI) =
      $.modState(State._password.set(e.target.value))

    def handleSubmit(e: ReactEventI) = {

    }
  }

  val component = ReactComponentB[Unit]("Login")
    .initialState(State(None, None/*Credentials("","")*/))
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
