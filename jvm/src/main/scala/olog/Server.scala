package olog

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

import upickle.default.{Reader, Writer}
object AutowireServer extends autowire.Server[String, Reader, Writer]{
  def read[Result: Reader](p: String) = upickle.default.read[Result](p)
  def write[Result: Writer](r: Result) = upickle.default.write(r)
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
        HttpEntity(MediaTypes.`text/html`, Views.homepage)
      }
    } ~
      getFromResourceDirectory("")
  } ~
    (post & entity(as[String])) { entity =>
      path("api" / Segments){ s =>
        complete {
          ToResponseMarshallable(
          AutowireServer.route[Api](AkkaHttpMicroservice)(
            autowire.Core.Request(s, AutowireServer.read[Map[String, String]](entity))
          ))
        }
      }
    } ~
    (post & entity(as[String])) { entity =>
      path("todoapi" / Segments){ s =>
        complete {
          ToResponseMarshallable(
            AutowireServer.route[TodoApi](AkkaHttpMicroservice)(
              autowire.Core.Request(s, AutowireServer.read[Map[String, String]](entity))
            ))
        }
      }
    }

  import database.AccountModel

  def users(user:Account.Session): Future[Seq[Account.Info]] = {
    AccountModel.list2.map(_.map(Account.Info.from))
  }

  def create(user:Account.User):Future[Created[Account.User]] = {
    AccountModel.create(user).map(created => Account.User._id set Some(created.value) apply user).map(e => Created(e))
  }

  def delete(id:Id[Account.User]):Future[Deleted[Account.User]] = {
    AccountModel.delete(id)
  }

  def updateLastname(id:Int, lastname:String):Future[Account.User] = {
    AccountModel.fetchThenUpdate(Id(id), Account.User._lastName.set(lastname))
  }

  def approve(id:Int):Future[Account.User] = {
    AccountModel.fetchThenUpdate(Id(id), Account.User._status.set(Account.Approved))
  }

  var todos = List[TodoItem](TodoItem("ines"))

  def all():Future[Seq[TodoItem]] = Future {
    todos
  }

  def create(item:TodoItem):Future[TodoItem] = Future {
    todos = item :: todos
    item
  }

  def login(credentials:Account.Credentials):Future[Option[Account.Session]] = {
    Future(Some(Account.Session(id = Id(1))))
  }

}
