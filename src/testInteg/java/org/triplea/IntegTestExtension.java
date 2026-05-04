package org.triplea;

import com.github.database.rider.core.configuration.GlobalConfig;
import com.github.database.rider.junit5.DBUnitExtension;
import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalConnectionFactoryConfiguration;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.quarkus.arc.Arc;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

@ExtendWith(DBUnitExtension.class)
public class IntegTestExtension implements BeforeAllCallback, BeforeEachCallback {

  private static Jdbi jdbi;
  private static boolean dbUnitConfigured = false;

  @Override
  public void beforeAll(final ExtensionContext context) {
    // intentionally empty — CDI container is not yet available at beforeAll time;
    // initialization is deferred to beforeEach, which runs after Quarkus starts
  }

  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    // CDI container is live by beforeEach; initialize once across all tests
    if (jdbi == null) {
      jdbi = Arc.container().select(Jdbi.class).get();
    }
    if (!dbUnitConfigured) {
      final AgroalDataSource dataSource = Arc.container().select(AgroalDataSource.class).get();
      final AgroalConnectionFactoryConfiguration factory =
          dataSource
              .getConfiguration()
              .connectionPoolConfiguration()
              .connectionFactoryConfiguration();

      final String url = factory.jdbcUrl();
      final String user =
          factory.credentials().stream()
              .filter(NamePrincipal.class::isInstance)
              .map(c -> ((NamePrincipal) c).getName())
              .findFirst()
              .orElse("lobby_user");
      final String password =
          factory.credentials().stream()
              .filter(SimplePassword.class::isInstance)
              .map(c -> new String(((SimplePassword) c).getWord()))
              .findFirst()
              .orElse("lobby_user");

      GlobalConfig.instance()
          .getDbUnitConfig()
          .url(url)
          .user(user)
          .password(password)
          .driver("org.postgresql.Driver");
      dbUnitConfigured = true;
    }

    final URL cleanupFileUrl = getClass().getClassLoader().getResource("db-cleanup.sql");
    if (cleanupFileUrl != null) {
      final String cleanupSql = Files.readString(Path.of(cleanupFileUrl.toURI()));
      jdbi.withHandle(handle -> handle.execute(cleanupSql));
    }
  }
}
