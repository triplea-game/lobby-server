package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.game.hosting.request.GameHostingResponse;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class GameHostingControllerTest extends ControllerIntegrationTest {

  private static final String GAME_HOSTING_PATH = "/lobby/game-hosting-request";

  LobbyHttpClientHelper client;

  @BeforeEach
  void setup() {
    client = new LobbyHttpClientHelper(localhost);
  }

  @Test
  void sendGameHostingRequest() {
    final var result = client.post(GAME_HOSTING_PATH, null, GameHostingResponse.class);
    assertThat(result, is(IsNull.notNullValue()));
  }
}
