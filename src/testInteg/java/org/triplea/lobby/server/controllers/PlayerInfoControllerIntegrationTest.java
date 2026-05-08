package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.moderator.PlayerSummary;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class PlayerInfoControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String FETCH_PLAYER_INFO_PATH = "/lobby/fetch-player-info";

  LobbyHttpClientHelper client;
  LobbyHttpClientHelper moderatorClient;

  @BeforeEach
  void setup() {
    client = new LobbyHttpClientHelper(localhost, ANONYMOUS);
    moderatorClient = new LobbyHttpClientHelper(localhost, MODERATOR);
  }

  @Test
  @Disabled
  /*
  Disabled: non-deterministic test, needs work.
  Failure message:
  Reason: Bad Request
  Http 400 - Bad Request: Player could not be found, have they left chat?
  */
  void fetchPlayerInfo() {
    final PlayerSummary playerSummary =
        client.post(FETCH_PLAYER_INFO_PATH, "chatter-chat-id2", PlayerSummary.class);

    assertThat(playerSummary.getCurrentGames(), is(notNullValue()));
    assertThat(playerSummary.getIp(), is(nullValue()));
    assertThat(playerSummary.getSystemId(), is(nullValue()));
  }

  @Test
  @Disabled
  /*
  Disabled: non-deterministic test, needs work.
  Failure message:
  Reason: Bad Request
  Http 400 - Bad Request: Player could not be found, have they left chat?
  */
  void fetchPlayerInfoAsModerator() {
    final PlayerSummary playerSummary =
        moderatorClient.post(FETCH_PLAYER_INFO_PATH, "chatter-chat-id2", PlayerSummary.class);

    assertThat(playerSummary.getIp(), is(notNullValue()));
    assertThat(playerSummary.getRegistrationDateEpochMillis(), is(notNullValue()));
    assertThat(playerSummary.getAliases(), is(notNullValue()));
    assertThat(playerSummary.getBans(), is(notNullValue()));
    assertThat(playerSummary.getSystemId(), is(notNullValue()));
  }
}
