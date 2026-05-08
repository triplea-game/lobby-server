package org.triplea.lobby.server.controllers;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class MuteUserControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String MUTE_USER_PATH = "/lobby/moderator/mute-player";

  LobbyHttpClientHelper client;

  @BeforeEach
  void setup() {
    this.client = new LobbyHttpClientHelper(localhost, MODERATOR);
  }

  @Test
  void muteUser() {
    client.post(MUTE_USER_PATH, Map.of("playerChatId", "chat-id", "minutes", 600));
  }
}
