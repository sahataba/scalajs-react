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
    def createUser(user:User):Future[Unit] = {
      Client[example.Api].createUser(user).call().map(u => store() = store().copy(users = store().users ++ List(u)))
    }
    def removeUser(user:User) = {
      store() = store().copy(users = users.filter( _ != user))
    }
  }

  Client[example.Api].users().call().map(_.toList).map{t =>
    store() = store().copy(users = t)
  }


  val store = Var(AppState(List()));

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
    ul(P.map{(u:User) => TodoItem(u)})
  })
  .build

  val TodoItem = ReactComponentB[User]("TodoItem")
  .render(P => {
    li(button(onclick ==> handleSubmit2(P))("X"),"Name: " + P.name, "Email: " + P.email)
  })
  .build

  //case class State(usr:User)

  class Backend(t: BackendScope[AppState, User]) {
    def onChangeName(e: ReactEventI) = {
      t.modState(user => user.copy(name = e.target.value))
    }
    def onChangeEmail(e: ReactEventI) = {
      t.modState(user => user.copy(email = e.target.value))
    }

    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      console.log("KK" + t.state)
      //a() = a().copy(users = a().users ++ List(User(t.state.text)))
      store().createUser(t.state).foreach(_ => t.modState(s => dummyUser))
    }
  }

  val myRef = Ref[HTMLInputElement]("refKey")

  val dummyUser = User(id = None, name = "", email = "")

  val TodoApp = ReactComponentB[AppState]("TodoApp")
    .initialState(dummyUser)
    .backend(new Backend(_))
    .render((P, S, B) =>
      div(
        h3("TODO"),
        TodoList(P.users),
        form(onsubmit ==> B.handleSubmit)(
          input(id:= "refKey", onchange ==> B.onChangeName, value := S.name),
          input(id:= "refKeyEmail", onchange ==> B.onChangeEmail, value := S.email),
          button("Add #", P.users.length + 1)
        )
      )
    ).build

}
