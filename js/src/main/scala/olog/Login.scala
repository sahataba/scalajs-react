package olog

import japgolly.scalajs.react._, vdom.prefix_<^._

object LoginPage {

  import Account.{Credentials}

  val component = ReactComponentB[Unit]("Login")
    .initialState(State.UserS(None, Some(Credentials("",""))))
    .backend(new State.UserS.Backend(_))
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
