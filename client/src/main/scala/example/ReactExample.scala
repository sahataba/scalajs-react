import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom.{HTMLInputElement, console, document, window, Node}

import scala.concurrent.{Future,Await}
import scala.concurrent.duration._
import scalajs.concurrent.JSExecutionContext.Implicits.runNow

import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._

import example._

import upickle._
import autowire._

import rx._

import monocle.syntax._

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
      Client[example.Api].
        createUser(user).
        call().
        map(u => store() = store().copy(users = store().users ++ List(u)))
    }
    def removeUser(user:User) = {
      Client[example.Api].
        removeUser(user.id.get).
        call().
        map(_ => store() = store().copy(users = users.filter( _ != user)))
    }
  }

  Client[example.Api].users().call().map(_.toList).map{t =>
    store() = store().copy(users = t)
  }

  /*
  object dispatcher {
    var state:AppState = Await.result(Client[example.Api].users().call().map(_.toList).map(users => AppState(users = users)), 2 seconds)
    console.log("TTTT " + state)
  }*/

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
      //dispatcher.state
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
    table(border:="1px solid black;")(P.map{(u:User) => TodoItem(u)})
  })
  .build

  val TodoItem = ReactComponentB[User]("TodoItem")
  .render(P => {
    tr(td(button(onClick ==> handleSubmit2(P))("X")),td("ID: " + P.id, "Name: " + P.name, "Email: " + P.email, P.birthday.toString, "Role: " + P.role))
  })
  .build

  //case class State(usr:User)

  class Backend(t: BackendScope[AppState, User]) {
    def onChangeName(e: ReactEventI) = {

      t.modState(user => user applyLens UserLenses.name set e.target.value)
    }
    def onChangeEmail(e: ReactEventI) = {
      t.modState(user => user applyLens UserLenses.email set e.target.value)
    }

    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      console.log("KK" + t.state)
      //a() = a().copy(users = a().users ++ List(User(t.state.text)))
      store().createUser(t.state).foreach(_ => t.modState(s => User.dummy()))
    }
  }

  val myRef = Ref[HTMLInputElement]("refKey")

  val TodoApp = ReactComponentB[AppState]("TodoApp")
    .initialState(User.dummy())
    .backend(new Backend(_))
    .render((P, S, B) =>
      div(
        h3("TODO"),
        TodoList(P.users),
        form(onSubmit ==> B.handleSubmit)(
          input(id:= "refKey", onChange ==> B.onChangeName, value := S.name),
          input(id:= "refKeyEmail", onChange ==> B.onChangeEmail, value := S.email),
          button("Add #", P.users.length + 1)
        )
      )
    ).build

}
