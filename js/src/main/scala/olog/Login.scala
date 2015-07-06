package olog

import japgolly.scalajs.react._, vdom.prefix_<^._

object LoginPage {

  case class State(user: Option[UserSession], credentials:Credentials)

  class Backend($: BackendScope[Unit, State]) {
    def onChangeEmail(e: ReactEventI) =
      $.modState(st => st.copy(credentials = Credentials._email.set(e.target.value)(st.credentials)))

    def onChangePassword(e: ReactEventI) =
      $.modState(st => st.copy(credentials = Credentials._password.set(e.target.value)(st.credentials)))

    def handleSubmit(e: ReactEventI) = {

    }
  }

  val component = ReactComponentB[Unit]("Login")
    .initialState(State(None, Credentials("","")))
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
                <.input(^.id := "password", ^.`type` := "password", ^.cls := "validate",  ^.onChange ==> B.onChangePassword, ^.value := S.credentials.password),
                <.label(^.`for` := "password", "Password")
              )
            ),
            <.div(
              ^.cls := "row",
              <.div(
                ^.cls := "input-field col s12",
                <.input(^.id := "email", ^.`type` := "email", ^.cls := "validate", ^.onChange ==> B.onChangeEmail, ^.value := S.credentials.email),
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
