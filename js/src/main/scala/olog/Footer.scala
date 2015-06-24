package olog

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import scalacss.ScalaCssReact._

object Footer {
  
  val component = ReactComponentB.static("Footer",
    <.footer(Style.footer,
    <.div(
      ^.cls := "container",
      <.p(^.paddingTop := "5px", "OLOG, obrt za informaticko savjetovanje"))
    )
  ).buildU

  def apply() = component()
}
