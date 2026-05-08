package org.triplea.lobby.server.controllers;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class GameListingControllerTest extends ControllerIntegrationTest {

  private static final String FETCH_GAMES_PATH = "/lobby/games/fetch-games";
  private static final String BOOT_GAME_PATH = "/lobby/games/boot-game";

  LobbyHttpClientHelper client;

  @BeforeEach
  void setup() {
    client = new LobbyHttpClientHelper(localhost, MODERATOR);
  }

  @Test
  void fetchGames() {
    client.get(FETCH_GAMES_PATH, Object[].class);
  }

  @Test
  void bootGame() {
    client.post(BOOT_GAME_PATH, "game-id");
  }
}
