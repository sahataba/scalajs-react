package olog

import japgolly.scalajs.react._, vdom.prefix_<^._

object LoginPage {

  case class State(user: Option[UserSession], credentials:Credentials)

  class Backend($: BackendScope[Unit, State]) {
    def onChangeEmail(e: ReactEventI) =
      $.modState(st => st.copy(credentials = Credentials._email.set(e.target.value)(st.credentials)))
  }

  val component = ReactComponentB[Unit]("Login")
    .render(P => {
    <.div(
      <.form(
        ^.cls := "col s12",
        <.div(
          ^.cls := "row",
          <.div(
            ^.cls := "row",
            <.div(
              ^.cls := "input-field col s12",
              <.input(^.id := "password", ^.`type` := "password", ^.cls := "validate"),
              <.label(^.`for` := "password", "Password")
            )
          ),
          <.div(
            ^.cls := "row",
            <.div(
              ^.cls := "input-field col s12",
              <.input(^.id := "email", ^.`type` := "email", ^.cls := "validate"),
              <.label(^.`for` := "email", "Email")
            )
          )
        )
      )
    )
  }).buildU

  def apply() = component()

}
