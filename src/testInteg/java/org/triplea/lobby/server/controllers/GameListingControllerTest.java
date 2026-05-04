package org.triplea.lobby.server.controllers;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.game.lobby.watcher.GameListingClient;
import org.triplea.lobby.server.ControllerIntegrationTest;

@QuarkusTest
public class GameListingControllerTest extends ControllerIntegrationTest {
  GameListingClient client;

  @BeforeEach
  void setup() {
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
