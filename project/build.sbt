
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.4")

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.1")

addSbtPlugin("com.lihaoyi" % "scalatex-sbt-plugin" % "0.3.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.2")

resolvers += "Flyway" at "http://flywaydb.org/repo"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers  += "Online Play Repository" at "http://repo.typesafe.com/typesafe/simple/maven-releases/"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("com.lihaoyi" % "utest-js-plugin" % "0.2.5-RC1")

addSbtPlugin("com.lihaoyi" % "workbench" % "0.2.3")

addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.14")
