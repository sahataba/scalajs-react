package olog

import japgolly.scalajs.react._, vdom.prefix_<^._

object AboutPage {

  val component = ReactComponentB[Unit]("About")
    .render(P => {
      <.div(<.h1("Olog"))})
    .buildU

  def apply() = component()

}
