package org.triplea.lobby.server.controllers;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.triplea.domain.data.PlayerChatId;
import org.triplea.http.client.lobby.moderator.ModeratorLobbyClient;
import org.triplea.lobby.server.ControllerIntegrationTest;

@QuarkusTest
public class DisconnectUserControllerIntegrationTest extends ControllerIntegrationTest {
  private static final PlayerChatId CHAT_ID = PlayerChatId.of("chat-id");
  ModeratorLobbyClient client;

  @BeforeEach
  void setup() {
    this.client = ModeratorLobbyClient.newClient(localhost, MODERATOR);
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        NOT_MODERATORS,
        apiKey -> ModeratorLobbyClient.newClient(localhost, apiKey),
        client -> client.disconnectPlayer("chat-id"));
  }

  @Test
  @DisplayName("Send disconnect request, verify we get a 400 for chat-id not found")
  void disconnectPlayer() {
    assertBadRequest(() -> client.disconnectPlayer(CHAT_ID.getValue()));
  }
}
