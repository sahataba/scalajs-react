package olo.database

import org.flywaydb.core.Flyway

object SchemaUpdater {

  def runSchemaUpdate(clean: Boolean = false): Unit = {
    val flyway = new Flyway
    flyway.setDataSource("jdbc:postgresql:todos", "postgres", "")
    flyway.setSchemas("public")
    if (clean) flyway.clean()
    flyway.migrate()
  }
}