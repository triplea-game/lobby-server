package org.triplea.modules.error.reporting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import java.net.URI;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.LobbyHttpClientConfig;
import org.triplea.http.client.error.report.CanUploadRequest;
import org.triplea.http.client.error.report.ErrorReportClient;
import org.triplea.http.client.error.report.ErrorReportRequest;
import org.triplea.http.client.error.report.ErrorReportResponse;

@Slf4j
class ErrorReportControllerIntegrationTest {
  private final ErrorReportClient client;

  ErrorReportControllerIntegrationTest() {
    log.info("System props: " + System.getProperties());
    log.info("Env vars: " + System.getenv());

    String host = Optional.ofNullable(System.getenv("LOBBY_HOST")).orElse("localhost");
    String port = Optional.ofNullable(System.getenv("LOBBY_TCP_8080")).orElse("8080");
    log.info("Using host: {}:{}", host, port);
    final URI localhost = URI.create(String.format("http://%s:%s", host, port));
    LobbyHttpClientConfig.setConfig(
        LobbyHttpClientConfig.builder().clientVersion("2.7").systemId("system").build());
    client = ErrorReportClient.newClient(localhost);
  }

  @Test
  void uploadErrorReport() {
    final ErrorReportResponse response =
        client.uploadErrorReport(
            ErrorReportRequest.builder()
                .body("body")
                .title("error-report-title-" + String.valueOf(Math.random()).substring(0, 10))
                .gameVersion("version")
                .build());

    assertThat(response.getGithubIssueLink(), is(notNullValue()));
  }

  @Test
  void canUploadErrorReport() {
    client.canUploadErrorReport(
        CanUploadRequest.builder().gameVersion("2.0").errorTitle("title").build());
  }
}
