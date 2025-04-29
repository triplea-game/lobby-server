package org.triplea.spitfire.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.triplea.domain.data.UserName;
import org.triplea.http.client.lobby.game.lobby.watcher.GamePostingRequest;
import org.triplea.http.client.lobby.game.lobby.watcher.LobbyWatcherClient;
import org.triplea.spitfire.server.ControllerIntegrationTest;
import org.triplea.spitfire.server.TestData;

// @DBRider
//// @ExtendWith(DbRi)
// @ExtendWith(DBUnitExtension.class)
// @DBUnit(url = System.getProperty("123"))
class LobbyWatcherControllerTest extends ControllerIntegrationTest {
  private static final GamePostingRequest GAME_POSTING_REQUEST =
      GamePostingRequest.builder().playerNames(List.of()).lobbyGame(TestData.LOBBY_GAME).build();

  private final URI localhost;
  private final LobbyWatcherClient client;

  LobbyWatcherControllerTest(final URI localhost) {
    this.localhost = localhost;
    client = LobbyWatcherClient.newClient(localhost, HOST);
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        NOT_HOST,
        apiKey -> LobbyWatcherClient.newClient(localhost, apiKey),
        client -> client.removeGame("game"),
        client -> client.postGame(GAME_POSTING_REQUEST),
        client -> client.sendKeepAlive("game-id"),
        client -> client.playerJoined("game-id", UserName.of("player-0")),
        client -> client.playerLeft("game-id", UserName.of("player-0")));
  }

  @Test
  void postGame() {
    client.postGame(GAME_POSTING_REQUEST);
  }

  @Test
  void removeGame() {
    client.removeGame("game-id");
  }

  @Test
  void keepAlive() {
    final boolean result = client.sendKeepAlive("game-id");
    assertThat(result, is(false));
  }

  @Test
  void updateGame() {
    client.postGame(GAME_POSTING_REQUEST);
  }

  @Test
  void notifyPlayerJoined() {
    client.playerJoined("game-id", UserName.of("player-0"));
  }

  @Test
  void notifyPlayerLeft() {
    client.playerJoined("game-id", UserName.of("player-1"));
  }
}
