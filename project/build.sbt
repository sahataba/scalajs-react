
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.3")

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.1")

addSbtPlugin("com.lihaoyi" % "scalatex-sbt-plugin" % "0.3.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.8.0")

resolvers += "Flyway" at "http://flywaydb.org/repo"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers  += "Online Play Repository" at "http://repo.typesafe.com/typesafe/simple/maven-releases/"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.1")

//addSbtPlugin("com.lihaoyi" % "utest-js-plugin" % "0.2.4")

addSbtPlugin("com.lihaoyi" % "workbench" % "0.2.3")

