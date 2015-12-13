import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging

enablePlugins(JavaServerAppPackaging)

name := "Olog"

scalaVersion := "2.11.7"

resolvers += Resolver.mavenLocal

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

val scOptions = Seq(
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  "-Xlint",
  "-Xfatal-warnings",
  //"-Ywarn-unused",
  "-Ywarn-unused-import",
  "-feature")

lazy val root = project.
  aggregate(fooJS, fooJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

val warningWarts = List(
  Wart.Any,
  Wart.Throw,
  Wart.Nothing,
  Wart.EitherProjectionPartial,
  Wart.DefaultArguments,
  Wart.AsInstanceOf,
  Wart.Product,
  Wart.ExplicitImplicitTypes,
  Wart.NonUnitStatements,
  Wart.Serializable,
  Wart.Var,
  //Wart.FinalCaseClass,
  Wart.IsInstanceOf,
  Wart.OptionPartial
)

lazy val p1 =
  crossProject.
  in(file(".")).
  settings(
    name := "olog",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.3.0",
      "com.lihaoyi" %%% "autowire" % "0.2.5",
      "com.lihaoyi" %%% "scalatags" % "0.5.2",
      "com.lihaoyi" %%% "scalarx" % "0.2.8",
      "com.lihaoyi" %%% "utest" % "0.3.1",
      "com.lihaoyi" %%% "pprint" % "0.3.0",
      "eu.timepit" %%% "refined" % "0.1.2",
      "org.spire-math" %%% "cats" % "0.2.0"
    )
  ).
  jvmSettings(
    scalacOptions ++= scOptions,
    libraryDependencies ++= akkahttpdependencies ++ Seq(
      "org.webjars" % "bootstrap" % "3.2.0",
      "org.tpolecat" %% "doobie-core" % "0.2.3",
      "org.tpolecat" %% "doobie-contrib-postgresql" % "0.2.3",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.h2database" % "h2" % "1.3.170",
      "org.flywaydb" % "flyway-core" % "3.0",
      "com.github.julien-truffaut"  %%  "monocle-core" % "1.1.1",
      "com.github.julien-truffaut"  %%  "monocle-macro" % "1.1.1"
    ),
    wartremoverErrors ++= Warts.allBut(warningWarts:_*)
  ).
  jsSettings(
      scalacOptions ++= scOptions,
      libraryDependencies ++= {
      val monocleV = "1.1.1"
      val reactV = "0.9.2"
      Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.8.1",
        "com.github.japgolly.scalajs-react" %%% "core" % reactV,
        "com.github.japgolly.scalajs-react" %%% "extra" % reactV,
        "com.github.japgolly.fork.monocle" %%% "monocle-core" % monocleV,
        "com.github.japgolly.fork.monocle" %%% "monocle-macro" % monocleV,
        "com.github.japgolly.scalacss" %%% "core" % "0.3.0",
        "com.github.japgolly.scalacss" %%% "ext-react" % "0.3.0"
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
