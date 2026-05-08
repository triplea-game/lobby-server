package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class RemoteActionsControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String SEND_SHUTDOWN_PATH = "/lobby/remote/actions/send-shutdown";
  private static final String IS_PLAYER_BANNED_PATH = "/lobby/remote/actions/is-player-banned";

  LobbyHttpClientHelper client;
  LobbyHttpClientHelper hostClient;

  @BeforeEach
  void setup() {
    client = new LobbyHttpClientHelper(localhost, MODERATOR);
    hostClient = new LobbyHttpClientHelper(localhost, HOST);
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        NOT_MODERATORS,
        apiKey -> new LobbyHttpClientHelper(localhost, apiKey),
        c -> c.post(SEND_SHUTDOWN_PATH, "game-id"));

    assertNotAuthorized(
        NOT_HOST,
        apiKey -> new LobbyHttpClientHelper(localhost, apiKey),
        c -> c.post(IS_PLAYER_BANNED_PATH, "3.3.3.3", Boolean.class));
  }

  @Test
  void sendShutdownSignal() {
    client.post(SEND_SHUTDOWN_PATH, "game-id");
  }

  @Test
  @DisplayName("IP address is banned")
  void userIsBanned() {
    final boolean result = hostClient.post(IS_PLAYER_BANNED_PATH, "1.1.1.1", Boolean.class);

    assertThat(result, is(true));
  }

  @Test
  @DisplayName("IP address has an expired ban")
  void userWasBanned() {
    final boolean result = hostClient.post(IS_PLAYER_BANNED_PATH, "1.1.1.2", Boolean.class);

    assertThat(result, is(false));
  }

  @Test
  @DisplayName("IP address is not in ban table at all")
  void userWasNeverBanned() {
    final boolean result = hostClient.post(IS_PLAYER_BANNED_PATH, "1.1.1.3", Boolean.class);

    assertThat(result, is(false));
  }
}
