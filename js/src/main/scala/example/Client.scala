package example

import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExport
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import org.scalajs.dom.{console}
import org.scalajs.dom.ext._

@JSExport
object Client extends autowire.Client[String, upickle.Reader, upickle.Writer]{
  override def doCall(req: Request): Future[String] = {
    console.log("miha")
    Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = upickle.write(req.args)
    ).map(_.responseText)
  }

  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}