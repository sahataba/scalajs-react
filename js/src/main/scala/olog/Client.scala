package olog

import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExport
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import org.scalajs.dom.{console}
import org.scalajs.dom.ext._
import upickle.default.{Reader, Writer}


@JSExport
object Client extends autowire.Client[String, Reader, Writer]{
  override def doCall(req: Request): Future[String] = {
    val url = "/api/" + req.path.mkString("/")
    val data = write(req.args)
    console.log(s"Request(url,data) ($url,$data)")
    Ajax.post(
      url = url,
      data = data
    ).map(_.responseText)
  }

  def read[Result: Reader](p: String) = upickle.default.read[Result](p)
  def write[Result: Writer](r: Result) = upickle.default.write(r)
}

@JSExport
object TodoClient extends autowire.Client[String, Reader, Writer]{
  override def doCall(req: Request): Future[String] = {
    Ajax.post(
      url = "/todoapi/" + req.path.mkString("/"),
      data = write(req.args)
    ).map(_.responseText)
  }

  def read[Result: Reader](p: String) = upickle.default.read[Result](p)
  def write[Result: Writer](r: Result) = upickle.default.write(r)
}