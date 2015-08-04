package olog

import japgolly.scalajs.react._, vdom.prefix_<^._
import scala.concurrent.ExecutionContext.Implicits.global
import autowire._
import chandu0101.scalajs.react.components.listviews._

object TodoApp {

  val data = List("ScalaJS","JavasScript","ReactJS","Html","Css","Software","Browser")

  val TodoList = ReactComponentB[List[Todo.Item]]("TodoList")
    .render(props =>
      {
        def createItem(item: Todo.Item) = <.li(item.description)
        <.ul(props map createItem)
      }).build

  case class State(items: List[Todo.Item], text: String)

  class Backend($: BackendScope[Unit, State]) {
    def onChange(e: ReactEventI) =
      $.modState(_.copy(text = e.target.value))

    def handleSubmit(e: ReactEventI) = {
      e.preventDefault()
      val item = Todo.Item($.get().text)
      TodoClient[olog.TodoApi].create(item).call().map(i => $.modState(s => s.copy(items = i :: s.items )))
    }

    def init() = for {
      items <- TodoClient[olog.TodoApi].all().call()
    } yield {
        $.modState(s => s.copy(items = items.toList))
      }
    init()

  }

  val TodoApp = ReactComponentB[Unit]("TodoApp")
    .initialState(State(Nil, ""))
    .backend(new Backend(_))
    .render((_, S, B) =>
      <.div(
        <.h3("TODO"),
          ReactListView(items = data, showSearchBox = true /*,onItemSelect = B.onItemSelect*/),
          TodoList(S.items),
        <.form(^.onSubmit ==> B.handleSubmit,
          <.input(^.onChange ==> B.onChange, ^.value := S.text),
          <.button("Add #", S.items.length + 1)
        )
      )
    )
    .buildU
}