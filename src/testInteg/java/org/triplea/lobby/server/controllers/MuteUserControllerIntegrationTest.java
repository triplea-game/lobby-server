package org.triplea.lobby.server.controllers;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.domain.data.PlayerChatId;
import org.triplea.http.client.lobby.moderator.ModeratorLobbyClient;
import org.triplea.lobby.server.ControllerIntegrationTest;

@QuarkusTest
public class MuteUserControllerIntegrationTest extends ControllerIntegrationTest {
  ModeratorLobbyClient client;

  @BeforeEach
  void setup() {
    this.client = ModeratorLobbyClient.newClient(localhost, MODERATOR);
  }

  @Test
  void muteUser() {
    client.muteUser(PlayerChatId.of("chat-id"), 600);
  }
}
