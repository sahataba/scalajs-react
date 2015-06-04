// Turn this project into a Scala.js project by importing these settings

import sbt.Keys._
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import ScalaJSKeys._
import com.lihaoyi.workbench.Plugin._
import spray.revolver.AppProcess
import spray.revolver.RevolverPlugin.Revolver

val cross = new utest.jsrunner.JsCrossBuild(
  scalaVersion := "2.11.4",
  version := "0.1-SNAPSHOT",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "upickle" % "0.2.5",
    "com.lihaoyi" %%% "autowire" % "0.2.3",
    "com.scalatags" %%% "scalatags" % "0.4.2",
    "com.scalarx" %%% "scalarx" % "0.2.6",
    "com.github.japgolly.nyaya" %%% "nyaya-core" % "0.5.0",
    "com.github.japgolly.nyaya" %%% "nyaya-test" % "0.5.0" % "test"
  )
)
val client = cross.js.in(file("client"))
                    .copy(id="client")
                    .settings(scalaJSSettings ++workbenchSettings:_*)
                    .settings(
  name := "Client",
  libraryDependencies ++= Seq(
    "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.7.0",
    "com.github.japgolly.fork.monocle" %%% "monocle-core" % "1.0.1",
    "com.github.japgolly.fork.monocle" %%% "monocle-macro" % "1.0.1"
  ),
  //jsDependencies += "org.webjars" % "react" % "0.12.1" / "react-with-addons.js" commonJSName "React",
  bootSnippet := "ReactExample().main();"
)

val akkahttpdependencies = {
  val akkaV       = "2.3.9"
  val akkaStreamV = "1.0-RC3"
  val scalaTestV  = "2.2.1"
  Seq(
    "com.typesafe.akka" %% "akka-actor"                        % akkaV,
    "com.typesafe.akka" %% "akka-stream-experimental"          % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-core-experimental"       % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental"            % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental"    % akkaStreamV,
    "org.scalatest"     %% "scalatest"                         % scalaTestV % "test"
  )
}

val server = cross.jvm.in(file("server"))
                    .copy(id="server")
                    .settings(Revolver.settings:_*)
                    .settings(
  name := "Server",
  libraryDependencies ++= akkahttpdependencies ++ Seq(
    "org.webjars" % "bootstrap" % "3.2.0",
    "com.typesafe.slick" %% "slick" % "3.0.0-RC1",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.h2database" % "h2" % "1.3.170",
    "org.flywaydb" % "flyway-core" % "3.0",
    "com.github.julien-truffaut"  %%  "monocle-core" % "1.0.1",
    "com.github.julien-truffaut"  %%  "monocle-macro" % "1.0.1"
  ),
  (resources in Compile) += {
    (fastOptJS in (client, Compile)).value
    (artifactPath in (client, Compile, fastOptJS)).value
  }
)
