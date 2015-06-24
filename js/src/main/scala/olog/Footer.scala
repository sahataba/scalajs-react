package olog

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import scalacss.ScalaCssReact._

object Footer {

  val component = ReactComponentB.static("Footer",
    <.footer(
      ^.cls:="page-footer",
      <.div(^.cls := "container","testing"),
      <.div(^.cls := "footer-copyright", "OLOG, obrt za informaticko savjetovanje")
    )
  ).buildU

  def apply() = component()
}
