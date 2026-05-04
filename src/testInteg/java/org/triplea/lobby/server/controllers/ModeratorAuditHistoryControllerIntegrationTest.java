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
import org.triplea.http.client.lobby.moderator.toolbox.log.ToolboxEventLogClient;
import org.triplea.lobby.server.ControllerIntegrationTest;

@QuarkusTest
public class ModeratorAuditHistoryControllerIntegrationTest extends ControllerIntegrationTest {
  ToolboxEventLogClient client;

  @BeforeEach
  void setup() {
    this.client = ToolboxEventLogClient.newClient(localhost, MODERATOR);
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        NOT_MODERATORS,
        apiKey -> ToolboxEventLogClient.newClient(localhost, apiKey),
        client ->
            client.lookupModeratorEvents(PagingParams.builder().pageSize(1).rowNumber(0).build()));
  }

  @Test
  void fetchHistory() {
    final List<ModeratorEvent> response =
        client.lookupModeratorEvents(PagingParams.builder().pageSize(1).rowNumber(0).build());
    assertThat(response, not(empty()));
    assertThat(response.get(0).getActionTarget(), notNullValue());
    assertThat(response.get(0).getModeratorAction(), notNullValue());
    assertThat(response.get(0).getDate(), notNullValue());
    assertThat(response.get(0).getModeratorName(), notNullValue());
  }
}
