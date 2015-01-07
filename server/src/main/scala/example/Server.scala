package example
import upickle._
import spray.routing.SimpleRoutingApp
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import spray.http.{MediaTypes, HttpEntity}

object Template{
  import scalatags.Text.all._
  import scalatags.Text.tags2.{title, style}

  val txt =
    "<!DOCTYPE html>" +
    html(
      head(
        title("Example Scala.js application"),
        meta(httpEquiv:="Content-Type", content:="text/html; charset=UTF-8"),
        script(`type`:="text/javascript", src:="/client-fastopt.js"),
        script(`type`:="text/javascript", src:="//localhost:12345/workbench.js"),
        script(`type` := "text/javascript", src:="http://fb.me/react-with-addons-0.12.1.js"),
        link(
          rel:="stylesheet",
          `type`:="text/css",
          href:="META-INF/resources/webjars/bootstrap/3.2.0/css/bootstrap.min.css"
        ),
        style("""
          .example-enter {
            opacity: 0.01;
            transition: opacity .5s ease-in;
          }
          .example-enter.example-enter-active {
            opacity: 1;
          }
          .example-leave {
            opacity: 1;
            transition: opacity .5s ease-in;
          }
          .example-leave.example-leave-active {
            opacity: 0.01;
          }
              """)
      ),
      body(margin:=0)(
        div(id:="eg1"),
        script("ReactExamples().main()")
      )
    )
}
object AutowireServer extends autowire.Server[String, upickle.Reader, upickle.Writer]{
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}
object Server extends SimpleRoutingApp with Api{
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    startServer("0.0.0.0", port = 8080) {
      get{
        pathSingleSlash {
          complete{
            HttpEntity(
              MediaTypes.`text/html`,
              Template.txt
            )
          }
        } ~
        getFromResourceDirectory("")
      } ~
      post {
        path("api" / Segments){ s =>
          extract(_.request.entity.asString) { e =>
            complete {
              AutowireServer.route[Api](Server)(
                autowire.Core.Request(s, upickle.read[Map[String, String]](e))
              )
            }
          }
        }
      }
    }
  }

  def users: Future[Seq[User]] = {
    TableModel.list2
  }

  def createUser(user:User):Future[User] = {
    TableModel.createUser(user).map(id => user.copy(id = Some(id)))
  }

  def removeUser(id:Int):Future[Unit] = {
   TableModel.removeUser(Some(id)).map(a => ())
  }
}
