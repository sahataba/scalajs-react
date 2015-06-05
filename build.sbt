name := "Olog"

scalaVersion := "2.11.6"

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

lazy val root = project.
  aggregate(fooJS, fooJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val p1 =
  crossProject.
  in(file(".")).
  settings(
    name := "olog",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.2.8",
      "com.lihaoyi" %%% "autowire" % "0.2.5",
      "com.lihaoyi" %%% "scalatags" % "0.5.2",
      "com.lihaoyi" %%% "scalarx" % "0.2.8",
      "com.lihaoyi" %%% "utest" % "0.3.1"
    )
  ).
  jvmSettings(
    libraryDependencies ++= akkahttpdependencies ++ Seq(
      "org.webjars" % "bootstrap" % "3.2.0",
      "com.typesafe.slick" %% "slick" % "3.0.0",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.h2database" % "h2" % "1.3.170",
      "org.flywaydb" % "flyway-core" % "3.0",
      "com.github.julien-truffaut"  %%  "monocle-core" % "1.0.1",
      "com.github.julien-truffaut"  %%  "monocle-macro" % "1.0.1"
    )
  ).
  jsSettings(
    libraryDependencies ++= {
      val monocleV = "1.1.1"
      Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.8.0",
        "com.github.japgolly.scalajs-react" %%% "core" % "0.9.0",
        "com.github.japgolly.fork.monocle" %%% "monocle-core" % monocleV,
        "com.github.japgolly.fork.monocle" %%% "monocle-macro" % monocleV
      )
    },
    bootSnippet := "ReactExample().main();"
  )


lazy val fooJVM =
  p1.jvm.
    settings(
      (resources in Compile) += {
        (fastOptJS in (p1.js, Compile)).value
        (artifactPath in (fooJS, Compile, fastOptJS)).value
      }
    ).
    settings(Revolver.settings:_*)

lazy val fooJS = p1.js

lazy val readme = scalatex.ScalatexReadme(
  projectId = "readme",
  wd = file(""),
  url = "https://github.com/lihaoyi/scalatex/tree/master",
  source = "Readme"
)
