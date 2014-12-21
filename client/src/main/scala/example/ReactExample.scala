import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom.{HTMLInputElement, console, document, window, Node}
import japgolly.scalajs.react._
import vdom.ReactVDom._
import all._

@JSExport
object ReactExamples extends {

  @JSExport
  def main(): Unit = {
    example1(document getElementById "eg1")
  }
  // ===================================================================================================================
  // Scala version of "A Simple Component" on http://facebook.github.io/react/
  def example1(mountNode: Node) = {
    val HelloMessage = ReactComponentB[String]("HelloMessage")
    .render(name => div("Hello ", name))
    .build
    React.renderComponent(HelloMessage("John"), mountNode)
  }
}
