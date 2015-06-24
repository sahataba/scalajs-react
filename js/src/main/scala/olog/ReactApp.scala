package olog

import org.scalajs.dom
import org.scalajs.dom.{console}


import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object ReactApp extends JSApp {

  @JSExport
  override def main(): Unit = {
    AppCSS.load
    console.log("CSS loaded")
    Pages.router().render(dom.document.body)
  }
}
