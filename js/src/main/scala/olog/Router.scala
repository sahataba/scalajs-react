package olog

import olog.TodoApp
import org.scalajs.dom
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._, vdom.prefix_<^._, ScalazReact._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router2._

import chandu0101.scalajs.react.components.fascades.LatLng
import chandu0101.scalajs.react.components.maps.GoogleMap

object Pages {

  sealed trait Page
  case object Home                 extends Page
  case object Doco                 extends Page
  case object About                 extends Page


  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    (trimSlashes
      | staticRoute(root,     Home) ~> render(<.div("hh"))
      | staticRoute("#doc",   Doco) ~> render(<.div(TodoApp.TodoApp()))
      | staticRoute("#about", About) ~> render(<.div(GoogleMap(width = 600 ,height = 500 ,center = LatLng(45.800403, 15.721538) , zoom = 8)))
      )
      .notFound(redirectToPage(Home)(Redirect.Replace))
      .renderWith(layout)
      .verify(Home, Doco)
  }

  def layout(c: RouterCtl[Page], r: Resolution[Page]) =
    <.div(
      navMenu(c),
      <.div(^.cls := "container", r.render()))

  val navMenu = ReactComponentB[RouterCtl[Page]]("Menu")
    .render { ctl =>
    def nav(name: String, target: Page) =
      <.li(
        ^.cls := "navbar-brand active",
        ctl setOnClick target,
        name)
    <.div(
      ^.cls := "navbar navbar-default",
      <.ul(
        ^.cls := "navbar-header",
        nav("Home",          Home),
        nav("About",         About),
        nav("Documentation", Doco)))
  }
    .configure(Reusability.shouldComponentUpdate)
    .build

  val baseUrl =
    if (dom.window.location.hostname == "localhost")
      BaseUrl.fromWindowOrigin_/
    else
      BaseUrl.fromWindowOrigin / "scalajs-react/"

  val router = Router(baseUrl, routerConfig.logToConsole)

}