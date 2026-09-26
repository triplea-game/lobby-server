package org.triplea.health;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

/**
 * Guards the SmallRye Health endpoints. The post-deploy checks rely on readiness, so the readiness
 * test pins that the Agroal datasource check is part of {@code /q/health/ready}, which is what
 * makes "healthy" dependency-aware rather than just "booted".
 */
@QuarkusTest
class HealthCheckIntegrationTest {

  @TestHTTPResource URI localhost;

  @Test
  void livenessIsUp() throws Exception {
    var response = get("/q/health/live");

    assertThat(response.body(), response.statusCode(), is(200));
  }

  @Test
  void readinessIsUpAndCoversTheDatabase() throws Exception {
    var response = get("/q/health/ready");

    assertThat(response.body(), response.statusCode(), is(200));
    // Same pattern the 'smoke' job in main.yml matches; keep the two in step.
    assertThat(response.body(), matchesPattern("(?s).*\"status\" *: *\"UP\".*"));
    // Quarkus Agroal's name for its datasource readiness check ('DataSourceHealthCheck').
    assertThat(response.body(), containsString("Database connections health check"));
  }

  private HttpResponse<String> get(String path) throws Exception {
    var request = HttpRequest.newBuilder().uri(localhost.resolve(path)).GET().build();
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
  }
}
