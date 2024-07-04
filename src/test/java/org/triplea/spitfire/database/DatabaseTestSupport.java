package org.triplea.spitfire.database;

public abstract class DatabaseTestSupport extends DbRiderTestExtension {
  @Override
  protected String getDatabaseUrl() {
    return "jdbc:postgresql://localhost:5432/lobby_db";
  }
}
