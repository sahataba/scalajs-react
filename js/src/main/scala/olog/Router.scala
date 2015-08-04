package olog

import olog.TodoApp
import org.scalajs.dom
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import japgolly.scalajs.react._, vdom.prefix_<^._, ScalazReact._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router2._
import scalacss.ScalaCssReact._

object Pages {

  sealed trait Page
  case object Home  extends Page
  case object Doco  extends Page
  case object About extends Page
  case object Login extends Page

  var isUserLoggedIn: Boolean = false

  val routerConfig = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._

    val privatePages = (emptyRule
      | staticRoute("#doc", Doco) ~> render(TodoApp.TodoApp())
      )
      .addCondition(isUserLoggedIn)(page => redirectToPage(Login)(Redirect.Push))

    (trimSlashes
      | staticRoute(root,     Home) ~> render(<.div(Style.content)("hh"))
      | staticRoute("#about", About) ~> render(<.div(AboutPage()))
      | staticRoute("#login", Login) ~> render(<.div(LoginPage()))
      | privatePages
      )
      .notFound(redirectToPage(Home)(Redirect.Replace))
      .renderWith(layout)
      .verify(Home, Doco)
  }

  def layout(c: RouterCtl[Page], r: Resolution[Page]) =
    <.div(
      navMenu(c),
      <.div(^.cls := "container", r.render()),
      Footer())

  val navMenu = ReactComponentB[RouterCtl[Page]]("Menu")
    .render { ctl =>
    def nav(name: String, target: Page) = <.li(<.a(ctl setOnClick target,name))
    <.nav(<.div(
      ^.cls := "nav-wrapper",
      <.a(^.cls:="brand-logo center","Logo"),
      <.ul(
        ^.cls := "right",
        nav("Home",          Home),
        nav("About",         About),
        isUserLoggedIn ?= nav("Documentation", Doco),
        nav("Login",         Login))))
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
