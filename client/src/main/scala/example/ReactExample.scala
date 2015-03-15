package example

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom.{console, document, Node}

import scala.concurrent.{Future,Await}
import scala.concurrent.duration._
import scalajs.concurrent.JSExecutionContext.Implicits.runNow

import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._

import autowire._

import rx._

import monocle.syntax._
import monocle._

import Addons.ReactCssTransitionGroup


@JSExport
object ReactExamples {

  @JSExport
  def main(): Unit = {
    example1(document getElementById "eg1")
  }

  case class AppState(user:UserSession, var users:List[User]) {
    def createUser(user:User):Future[Unit] = {
      Client[example.Api].
        create(user).
        call().
        map(u => store() = store().copy(users = store().users ++ List(u)))
    }
    def removeUser(user:User) = {
      Client[example.Api].
        delete(user.id.get).
        call().
        map(_ => store() = store().copy(users = users.filter( _ != user)))
    }
  }

  val store = Var(AppState(UserSession("rudi"), List()))

  Client[example.Api].users(store().user).call().map(_.toList).map{t =>
    store() = store().copy(users = t)
  }

  /*
  object dispatcher {
    var state:AppState = Await.result(Client[example.Api].users().call().map(_.toList).map(users => AppState(users = users)), 2 seconds)
    console.log("TTTT " + state)
  }*/


  def example1(mountNode: Node) = {
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
    ReactCssTransitionGroup("example",  component = "table")(
      thead(
        tr(
          th("Actions"),
          todoItemFields.map{case (name,lns) => th(name)}
        )
      ),
      P.map{TodoItem(_)}
    )
  })
  .build

  val todoItemFields:List[(String, User => String)] = List(
    ("id: ", (user:User) => User._id.get(user).toString),
    ("name: ", User._name get),
    ("email: ", (User._email composeLens Email._email) get),
    ("birthday: ", (user:User) => User._birthday.get(user).toString),
    ("status: ", (user:User) => User._status.get(user).toString),
    ("role: ", RoleConverter.write compose (User._role get)))

  def tds(user:User) = todoItemFields.map{case (name,lns) => td(lns(user))}

  val TodoItem = ReactComponentB[User]("TodoItem")
  .render(P => {
    tr(key := P.id.get.toString)(
      td(button(onClick ==> handleSubmit2(P))("X")),
      tds(P))
  })
  .build

  //case class State(usr:User)

  class Backend(t: BackendScope[AppState, User]) {
    def onFieldChange[F](f : EditField[User, F])(e: ReactEventI) = {

      f.parse(e.target.value) match {
        case Right(en) => t.modState(f.lens set en)
        case Left(_) => console.log("Invalid: " + e.target.value)
      }
    }

    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      console.log("KK" + t.state)
      //a() = a().copy(users = a().users ++ List(User(t.state.text)))
      store().createUser(t.state).foreach(_ => t.modState(s => User.dummy()))
    }
  }

  case class EditField[E, F](
                           label:String,
                           lens:Lens[E, F],
                           parse: String => Either[String, F],
                           write: F => String)

  object EditField {
    def value[E, F](entity:E, field:EditField[E, F]):String = field.write(field.lens.get(entity))
  }

  def ddParse = (v:String) => Right(v)
  def id[E](x:E) = x

  def inputs = List(
    EditField[User,String](label = "First Name: ", lens = User._firstName, parse = ddParse, write = id),
    EditField[User,String](label = "Last Name: ", lens = User._lastName, parse = ddParse, write = id),
    EditField[User,Email](label = "Email: ", lens = User._email, Email.parse _, write = Email._email get),
    EditField[User,Role](label = "Role: ", lens = User._role, parse = RoleConverter.read, write = RoleConverter.write))

  val TodoApp = ReactComponentB[AppState]("TodoApp")
    .initialState(User.dummy())
    .backend(new Backend(_))
    .render((P, S, B) =>
      div(
        h3("TODO"),
        TodoList(P.users),
        form(onSubmit ==> B.handleSubmit)(
          inputs map (field => div(label(field.label),input(onChange ==> B.onFieldChange(field), value := EditField.value(S, field)))),
          div(
            label("Role: "),
            select(onChange ==> B.onFieldChange(EditField(label = "Role", User._role, parse = RoleConverter.read, write = RoleConverter.write)), value := RoleConverter.write(S.role))(
              RoleConverter.roles.map(role => option(value:=RoleConverter.write(role))(role.toString))
            )
          ),
          button("Add #", P.users.length + 1)
        )
      )
    ).build

}
