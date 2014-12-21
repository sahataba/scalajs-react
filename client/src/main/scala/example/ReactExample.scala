import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom.{HTMLInputElement, console, document, window, Node}

import scala.concurrent.{Future,Await}
import scala.concurrent.duration._
import scalajs.concurrent.JSExecutionContext.Implicits.runNow

import japgolly.scalajs.react._
import vdom.ReactVDom._
import all._

import example._

import upickle._
import autowire._

@JSExport
object Client extends autowire.Client[String, upickle.Reader, upickle.Writer]{
  override def doCall(req: Request): Future[String] = {
    console.log("miha")
    org.scalajs.dom.extensions.Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = upickle.write(req.args)
    ).map(_.responseText)
  }

  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

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

  val TodoApp = ReactComponentB[Unit]("TodoApp")
    .getInitialState(p => {
      State(List(User("Iva"), User("Maja")), "")
    })
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
    ).componentDidMount(scope => {
      val fetched = Client[example.Api].list("").call().map(_.toList)
      fetched.map( users =>
        scope.modState(_ => State(users, ""))
      )
    }).buildU

}
