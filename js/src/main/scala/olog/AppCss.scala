package olog

import scalacss.Defaults._
import scalacss.ScalaCssReact._
import scalacss.mutable.GlobalRegistry
import scalacss.mutable.StyleSheet.Inline

object AppCSS {

  def load = {
    GlobalRegistry.register(Style)
    GlobalRegistry.onRegistration(_.addToDocument())
  }
}

object Style extends Inline {

  import dsl._

  val container = style(maxWidth(1024 px))


  val content = style(display.flex,
    padding(30.px),
    flexDirection.column,
    alignItems.center)
  val footer = style(
    backgroundColor.green,
    padding(35.px))
}
