package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.game.lobby.watcher.GamePostingRequest;
import org.triplea.http.client.lobby.game.lobby.watcher.GamePostingResponse;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;
import org.triplea.lobby.server.TestData;

@QuarkusTest
public class LobbyWatcherControllerTest extends ControllerIntegrationTest {

  private static final String POST_GAME_PATH = "/lobby/games/post-game";
  private static final String REMOVE_GAME_PATH = "/lobby/games/remove-game";
  private static final String KEEP_ALIVE_PATH = "/lobby/games/keep-alive";
  private static final String PLAYER_JOINED_PATH = "/lobby/games/player-joined";
  private static final String PLAYER_LEFT_PATH = "/lobby/games/player-left";

  private static final GamePostingRequest GAME_POSTING_REQUEST =
      GamePostingRequest.builder().playerNames(List.of()).lobbyGame(TestData.LOBBY_GAME).build();

  LobbyHttpClientHelper client;

  @BeforeEach
  void setup() {
    client = new LobbyHttpClientHelper(localhost, HOST);
  }

  private static Map<String, String> playerJoinedBody(
      final String gameId, final String playerName) {
    return Map.of("gameId", gameId, "playerName", playerName);
  }

  private static Map<String, String> playerLeftBody(final String gameId, final String playerName) {
    return Map.of("gameId", gameId, "playerName", playerName);
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        NOT_HOST,
        apiKey -> new LobbyHttpClientHelper(localhost, apiKey),
        c -> c.post(REMOVE_GAME_PATH, "game"),
        c -> c.post(POST_GAME_PATH, GAME_POSTING_REQUEST, GamePostingResponse.class),
        c -> c.post(KEEP_ALIVE_PATH, "game-id", Boolean.class),
        c -> c.post(PLAYER_JOINED_PATH, playerJoinedBody("game-id", "player-0")),
        c -> c.post(PLAYER_LEFT_PATH, playerLeftBody("game-id", "player-0")));
  }

  @Test
  void postGame() {
    client.post(POST_GAME_PATH, GAME_POSTING_REQUEST, GamePostingResponse.class);
  }

  @Test
  void removeGame() {
    client.post(REMOVE_GAME_PATH, "game-id");
  }

  @Test
  void keepAlive() {
    final boolean result = client.post(KEEP_ALIVE_PATH, "game-id", Boolean.class);
    assertThat(result, is(false));
  }

  @Test
  void updateGame() {
    client.post(POST_GAME_PATH, GAME_POSTING_REQUEST, GamePostingResponse.class);
  }

  @Test
  void notifyPlayerJoined() {
    client.post(PLAYER_JOINED_PATH, playerJoinedBody("game-id", "player-0"));
  }

  @Test
  void notifyPlayerLeft() {
    client.post(PLAYER_JOINED_PATH, playerJoinedBody("game-id", "player-1"));
  }
}
