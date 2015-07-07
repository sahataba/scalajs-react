package olog

import upickle._

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{MediaTypes, HttpResponse, HttpRequest, HttpEntity}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorFlowMaterializer, FlowMaterializer}
import akka.stream.scaladsl.{Flow, Sink, Source}

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import scala.concurrent.{ExecutionContextExecutor, Future}

object Template{
  import scalatags.Text.all._
  import scalatags.Text.tags2.{title, style}

  val txt =
    "<!DOCTYPE html>" +
    html(
      head(
        title("Example Scala.js application"),
        meta(httpEquiv:="Content-Type", content:="text/html; charset=UTF-8"),
        script(`type`:="text/javascript", src:="/olog-fastopt.js"),
        script(`type`:="text/javascript", src:="//localhost:12345/workbench.js"),
        script(`type` := "text/javascript", src:="http://fb.me/react-with-addons-0.12.1.js"),
        link(rel:="stylesheet",`type`:="text/css",href:="https://cdnjs.cloudflare.com/ajax/libs/materialize/0.97.0/css/materialize.min.css"),
        script(`type`:="text/javascript",src:="https://code.jquery.com/jquery-2.1.1.min.js"),
        script(`type`:="text/javascript",src:="https://cdnjs.cloudflare.com/ajax/libs/materialize/0.97.0/js/materialize.min.js"),
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
        script("olog.ReactApp().main()")
      )
    )
}
object AutowireServer extends autowire.Server[String, upickle.Reader, upickle.Writer]{
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

object AkkaHttpMicroservice extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, interface = config.getString("http.interface"), port = config.getInt("http.port"))//.startHandlingWith(routes)
}

trait Service extends Api with TodoApi{

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: FlowMaterializer

  def config: Config
  val logger: LoggingAdapter

  val routes = get{
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
    (post & entity(as[String])) { entity =>
      path("api" / Segments){ s =>
        complete {
          ToResponseMarshallable(
          AutowireServer.route[Api](AkkaHttpMicroservice)(
            autowire.Core.Request(s, upickle.read[Map[String, String]](entity))
          ))
        }
      }
    } ~
    (post & entity(as[String])) { entity =>
      path("todoapi" / Segments){ s =>
        complete {
          ToResponseMarshallable(
            AutowireServer.route[TodoApi](AkkaHttpMicroservice)(
              autowire.Core.Request(s, upickle.read[Map[String, String]](entity))
            ))
        }
      }
    }

  def users(user:Account.UserSession): Future[Seq[Account.User]] = {
    TableModel.list2
  }

  def create(user:Account.User):Future[Created[Account.User]] = {
    TableModel.create(user).map(created => Account.User._id set Some(created.value) apply user).map(e => Created(e))
  }

  def delete(id:Id[Account.User]):Future[Deleted[Account.User]] = {
   TableModel.delete(id)
  }

  def updateLastname(id:Int, lastname:String):Future[Account.User] = {
    TableModel.fetchThenUpdate(Id(id), Account.User._lastName.set(lastname))
  }

  def approve(id:Int):Future[Account.User] = {
    TableModel.fetchThenUpdate(Id(id), Account.User._status.set(Approved))
  }

  var todos = List[TodoItem](TodoItem("ines"))

  def all():Future[Seq[TodoItem]] = Future {
    todos
  }

  def create(item:TodoItem):Future[TodoItem] = Future {
    todos = item :: todos
    item
  }
}
