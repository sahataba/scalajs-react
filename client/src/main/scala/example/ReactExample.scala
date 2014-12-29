import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom.{HTMLInputElement, console, document, window, Node}

import scala.concurrent.{Future,Await}
import scala.concurrent.duration._
import scalajs.concurrent.JSExecutionContext.Implicits.runNow

import japgolly.scalajs.react._
import vdom.ReactVDom._
import japgolly.scalajs.react.vdom.ReactVDom.all._

import example._

import upickle._
import autowire._

import rx._

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

  case class AppState(var users:List[User]) {
    def createUser(name:String):Future[Unit] = {
      Client[example.Api].createUser(name).call().map(u => store() = store().copy(users = store().users ++ List(u)))
    }
    def removeUser(user:User) = {
      store() = store().copy(users = users.filter( _ != user))
    }
  }

  Client[example.Api].users().call().map(_.toList).map{t =>
    store() = store().copy(users = t)
  }


  val store = Var(AppState(List(User("rudi"), User("miha"))));

  // ===================================================================================================================
  // Scala version of "A Simple Component" on http://facebook.github.io/react/
  def example1(mountNode: Node) = {
    val HelloMessage = ReactComponentB[String]("HelloMessage")
    .render(name => div("Hello ", name))
    .build
    //React.renderComponent(TodoApp(), mountNode)
    Obs(store){
      console.log("render")
      React.renderComponent(TodoApp(store()), mountNode)
    }
  }


  //todo example
  def handleSubmit2(user:User)(e: ReactEventI) = {
    e.preventDefault()
    store().removeUser(user)
  }

  val TodoList = ReactComponentB[List[User]]("TodoList")
  .render(P => {
    def createItem(user: User) = li(button(onclick ==> handleSubmit2(user))("X"),user.name)
    ul(P map createItem)
  })
  .build


  case class State(text: String)

  class Backend(t: BackendScope[AppState, State]) {
    def onChange(e: ReactEventI) = {
      console.log("CC" + e.target)
      t.modState(_.copy(text = e.target.value))
    }
    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      console.log("KK" + t.state)
      //a() = a().copy(users = a().users ++ List(User(t.state.text)))
      store().createUser(t.state.text).foreach(_ => t.modState(s => State("b")))
    }
  }

  val myRef = Ref[HTMLInputElement]("refKey")

  val TodoApp = ReactComponentB[AppState]("TodoApp")
    .initialState(State(""))
    .backend(new Backend(_))
    .render((P, S, B) =>
      div(
        h3("TODO"),
        TodoList(P.users),
        form(onsubmit ==> B.handleSubmit)(
          input(id:= "refKey", onchange ==> B.onChange, value := S.text),
          button("Add #", P.users.length + 1)
        )
      )
    ).build

}
