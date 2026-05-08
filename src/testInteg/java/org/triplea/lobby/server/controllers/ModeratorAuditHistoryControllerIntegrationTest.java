package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.moderator.toolbox.PagingParams;
import org.triplea.http.client.lobby.moderator.toolbox.log.ModeratorEvent;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class ModeratorAuditHistoryControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String AUDIT_HISTORY_PATH = "/lobby/moderator-toolbox/audit-history/lookup";

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
                AUDIT_HISTORY_PATH,
                PagingParams.builder().pageSize(1).rowNumber(0).build(),
                ModeratorEvent.class));
  }

  @Test
  void fetchHistory() {
    final List<ModeratorEvent> response =
        client.postForList(
            AUDIT_HISTORY_PATH,
            PagingParams.builder().pageSize(1).rowNumber(0).build(),
            ModeratorEvent.class);
    assertThat(response, not(empty()));
    assertThat(response.get(0).getActionTarget(), notNullValue());
    assertThat(response.get(0).getModeratorAction(), notNullValue());
    assertThat(response.get(0).getDate(), notNullValue());
    assertThat(response.get(0).getModeratorName(), notNullValue());
  }
}
