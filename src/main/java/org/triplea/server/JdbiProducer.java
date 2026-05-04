package org.triplea.server;

import io.agroal.api.AgroalDataSource;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.triplea.lobby.server.JdbiLogging;

@ApplicationScoped
@Slf4j
public class JdbiProducer {

  @Inject AgroalDataSource dataSource;

  @ConfigProperty(name = "app.log-sql-statements", defaultValue = "false")
  boolean logSqlStatements;

  private Jdbi jdbi;

  void onStart(@Observes StartupEvent event) {
    // Eagerly create the Jdbi instance so that any startup-time wiring
    // (e.g. row-mapper registration) happens before the first request arrives.
    jdbi();
  }

  @Produces
  @ApplicationScoped
  public Jdbi jdbi() {
    if (jdbi == null) {
      jdbi = Jdbi.create(dataSource).installPlugin(new SqlObjectPlugin());
      if (logSqlStatements) {
        log.info("SQL statement logging is enabled");
        JdbiLogging.registerSqlLogger(jdbi);
      }
    }
    return jdbi;
  }
}
