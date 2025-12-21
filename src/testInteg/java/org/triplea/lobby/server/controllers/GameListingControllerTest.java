package org.triplea.lobby.server.controllers;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.game.lobby.watcher.GameListingClient;
import org.triplea.lobby.server.ControllerIntegrationTest;

class GameListingControllerTest extends ControllerIntegrationTest {
  private final GameListingClient client;

  GameListingControllerTest(final URI localhost) {
    client = GameListingClient.newClient(localhost, MODERATOR);
  }

  @Test
  void fetchGames() {
    client.fetchGameListing();
  }

  @Test
  void bootGame() {
    client.bootGame("game-id");
  }
}
