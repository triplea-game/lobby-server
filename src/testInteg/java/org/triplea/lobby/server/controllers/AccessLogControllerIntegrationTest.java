package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.moderator.toolbox.PagingParams;
import org.triplea.http.client.lobby.moderator.toolbox.log.AccessLogData;
import org.triplea.http.client.lobby.moderator.toolbox.log.AccessLogRequest;
import org.triplea.http.client.lobby.moderator.toolbox.log.AccessLogSearchRequest;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class AccessLogControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String ACCESS_LOG_PATH = "/lobby/moderator-toolbox/access-log";

  LobbyHttpClientHelper client;

  @BeforeEach
  void setup() {
    this.client = new LobbyHttpClientHelper(localhost, MODERATOR);
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        NOT_MODERATORS,
        apiKey -> new LobbyHttpClientHelper(localhost, apiKey),
        c ->
            c.postForList(
                ACCESS_LOG_PATH,
                accessLogRequest(
                    AccessLogSearchRequest.builder().username("username").ip("ip").build(),
                    PagingParams.builder().pageSize(1).rowNumber(0).build()),
                AccessLogData.class),
        c ->
            c.postForList(
                ACCESS_LOG_PATH,
                accessLogRequest(
                    AccessLogSearchRequest.EMPTY_SEARCH,
                    PagingParams.builder().pageSize(1).rowNumber(0).build()),
                AccessLogData.class));
  }

  @Test
  void getAccessLog() {
    final List<AccessLogData> result =
        client.postForList(
            ACCESS_LOG_PATH,
            accessLogRequest(
                AccessLogSearchRequest.EMPTY_SEARCH,
                PagingParams.builder().pageSize(1).rowNumber(0).build()),
            AccessLogData.class);

    assertThat(result, is(not(empty())));
    assertThat(result.get(0).getAccessDate(), is(notNullValue()));
    assertThat(result.get(0).getSystemId(), is(notNullValue()));
    assertThat(result.get(0).getUsername(), is(notNullValue()));
  }

  @Test
  void getAccessLogUnauthorizedCase() {
    assertNotAuthorized(
        NOT_MODERATORS,
        apiKey -> new LobbyHttpClientHelper(localhost, apiKey),
        c ->
            c.postForList(
                ACCESS_LOG_PATH,
                accessLogRequest(
                    AccessLogSearchRequest.EMPTY_SEARCH,
                    PagingParams.builder().pageSize(1).rowNumber(0).build()),
                AccessLogData.class));
  }

  /**
   * First we do a fetch of access log and we pick the first record. We then search by username and
   * IP address using data from the first record, which should guarantee that our search will return
   * at least that one result (if not more).
   */
  @Test
  void getAccessLogWithSearchParams() {
    final AccessLogData firstListing =
        client
            .postForList(
                ACCESS_LOG_PATH,
                accessLogRequest(
                    AccessLogSearchRequest.EMPTY_SEARCH,
                    PagingParams.builder().pageSize(1).rowNumber(0).build()),
                AccessLogData.class)
            .get(0);

    final List<AccessLogData> result =
        client.postForList(
            ACCESS_LOG_PATH,
            accessLogRequest(
                AccessLogSearchRequest.builder()
                    .username(firstListing.getUsername())
                    .ip(firstListing.getIp())
                    .build(),
                PagingParams.builder().pageSize(1).rowNumber(0).build()),
            AccessLogData.class);

    assertThat(
        "We expect there to have been at least one match for sure", result, is(not(empty())));
    assertThat(
        "Username should match what we searched for",
        result.get(0).getUsername(),
        is(firstListing.getUsername()));
    assertThat(
        "IP should match what we searched for", result.get(0).getIp(), is(firstListing.getIp()));
  }

  @Test
  void emptySearchShouldBeSameAsAllSearch() {
    final PagingParams paging = PagingParams.builder().pageSize(1).rowNumber(0).build();

    final List<AccessLogData> emptySearchResult =
        client.postForList(
            ACCESS_LOG_PATH,
            accessLogRequest(AccessLogSearchRequest.builder().build(), paging),
            AccessLogData.class);

    final List<AccessLogData> allSearchResult =
        client.postForList(
            ACCESS_LOG_PATH,
            accessLogRequest(AccessLogSearchRequest.EMPTY_SEARCH, paging),
            AccessLogData.class);

    assertThat(emptySearchResult, is(equalTo(allSearchResult)));
  }

  private static AccessLogRequest accessLogRequest(
      final AccessLogSearchRequest searchRequest, final PagingParams pagingParams) {
    return AccessLogRequest.builder()
        .accessLogSearchRequest(searchRequest)
        .pagingParams(pagingParams)
        .build();
  }
}
