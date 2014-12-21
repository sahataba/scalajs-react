import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom.{HTMLInputElement, console, document, window, Node}
import japgolly.scalajs.react._
import vdom.ReactVDom._
import all._

import example._

@JSExport
object ReactExamples {

  @JSExport
  def main(): Unit = {
    example1(document getElementById "eg1")
  }
  // ===================================================================================================================
  // Scala version of "A Simple Component" on http://facebook.github.io/react/
  def example1(mountNode: Node) = {
    val HelloMessage = ReactComponentB[String]("HelloMessage")
    .render(name => div("Hello ", name))
    .build
    React.renderComponent(TodoApp(), mountNode)
  }


  //todo example
  val TodoList = ReactComponentB[List[User]]("TodoList")
  .render(P => {
    def createItem(user: User) = li(user.name)
    ul(P map createItem)
  })
  .build

  case class State(users: List[User], text: String)

  class Backend(t: BackendScope[Unit, State]) {
    def onChange(e: ReactEventI) =
      t.modState(_.copy(text = e.target.value))
    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      t.modState(s => State(s.users :+ User(s.text), ""))
    }
  }

  //val initial = example.Client[example.Api].list("").toList

  val TodoApp = ReactComponentB[Unit]("TodoApp")
    .initialState(State(Nil, ""))
    .backend(new Backend(_))
    .render((_,S,B) =>
    div(
      h3("TODO"),
      TodoList(S.users),
      form(onsubmit ==> B.handleSubmit)(
        input(onchange ==> B.onChange, value := S.text),
        button("Add #", S.users.length + 1)
      )
    )
  ).buildU

}
