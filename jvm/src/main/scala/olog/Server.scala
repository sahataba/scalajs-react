package olog

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{MediaTypes, HttpResponse, HttpRequest, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorFlowMaterializer, FlowMaterializer}
import akka.stream.scaladsl.{Flow, Sink, Source}

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import olo.database.SchemaUpdater
import scala.concurrent.{ExecutionContextExecutor, Future}

import pprint.Config.Defaults._

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

  SchemaUpdater.runSchemaUpdate()

  Http().bindAndHandle(
    routes,
    interface = config.getString("http.interface"),
    port = config.getInt("http.port"))
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
      path("api" / Segments){ path =>
        complete {
          val data = AutowireServer.read[Map[String, String]](entity)
          logger.info(s"Request (url,data) ($path,$data)")
          ToResponseMarshallable(
          AutowireServer.route[Api](AkkaHttpMicroservice)(
            autowire.Core.Request(path, data)
          ))
        }
      }
    } ~
    (post & entity(as[String])) { entity =>
      path("todoapi" / Segments){ path =>
        complete {
          val data = AutowireServer.read[Map[String, String]](entity)
          logger.info(s"Request (url,data) ($path,$data)")
          ToResponseMarshallable(
            AutowireServer.route[TodoApi](AkkaHttpMicroservice)(
              autowire.Core.Request(path, data)
            ))
        }
      }
    }

  import database.db

  def users(user:User.Session): Future[List[User.Info]] = {
    db.list.map(_.map(User.Info.from))
  }

  def create(user:User.Record):Future[Created[User.Record]] = {
    db.
      create(user).
      map(created => User.Record._id set Some(created.value) apply user).
      map(Created(_))
  }

  def delete(id:User.Id):Future[Deleted[User.Record]] = {
    db.delete(id)
  }

  def updateLastname(id:User.Id, lastname:String):Future[User.Record] = {
    db.
      fetchThenUpdate(
        id = id,
        upd = User.Record._lastName.set(lastname))
  }

  def approve(id:User.Id):Future[User.Record] = {
    db.
      fetchThenUpdate(
        id,
        User.Record._status.set(User.Approved))
  }

  var todos = List[Todo.Item](Todo.Item("ines"))

  def all():Future[Seq[Todo.Item]] =
    for {
      todos <- db.all
    } yield {
      pprint.pprintln(todos, width = 5)
      todos
    }

  def create(item:Todo.Item):Future[Todo.Item] =
    for {
      i <- db.create(item.description)
    } yield item

  def login(credentials:User.Credentials):Future[Option[User.Session]] = {
    Future(Some(User.Session(id = new User.Id(1))))
  }

}
