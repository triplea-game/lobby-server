package org.triplea.lobby.server.controllers;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.triplea.domain.data.LobbyGame;
import org.triplea.http.client.lobby.AuthenticationHeaders;
import org.triplea.http.client.lobby.game.lobby.watcher.GamePostingRequest;
import org.triplea.http.client.lobby.game.lobby.watcher.GamePostingResponse;
import org.triplea.http.client.lobby.game.lobby.watcher.LobbyGameListing;
import org.triplea.http.client.lobby.game.lobby.watcher.LobbyWatcherClient;
import org.triplea.http.client.web.socket.client.connections.PlayerToLobbyConnection;
import org.triplea.http.client.web.socket.messages.envelopes.game.listing.LobbyGameRemovedMessage;
import org.triplea.http.client.web.socket.messages.envelopes.game.listing.LobbyGameUpdatedMessage;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.TestData;
import org.triplea.lobby.server.controllers.lobby.LobbyWatcherController;

/*
GameListingWebsocketIntegrationTest > Post a game, verify listener is notified FAILED
    Wanted but not invoked:
    gameUpdatedListener.accept(
        LobbyGameListing(gameId=690deee5-8cf7-4815-82b3-d0cc9424fa53,
        lobbyGame=LobbyGame(hostAddress=127.0.0.1, hostPort=12, hostName=name,
         mapName=map, playerCount=3, gameRound=1, epochMilliTimeStarted=1599358874438,
         mapVersion=1, passworded=false, status=Waiting For Players, comments=comments))
    );
    -> at org.triplea.modules.game.GameListingWebsocketIntegrationTest.verifyPostGame(
       GameListingWebsocketIntegrationTest.java:94)
    Actually, there were zero interactions with this mock.
        at org.triplea.modules.game.GameListingWebsocketIntegrationTest.verifyPostGame(
        GameListingWebsocketIntegrationTest.java:94)
 */
@Disabled // Disabled due to flakiness, the above error is frequently seen and needs to be resolved.
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class GameListingWebsocketIntegrationTest extends ControllerIntegrationTest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final GamePostingRequest GAME_POSTING_REQUEST =
      GamePostingRequest.builder().playerNames(List.of()).lobbyGame(TestData.LOBBY_GAME).build();

  @Mock private Consumer<LobbyGameListing> gameUpdatedListener;
  @Mock private Consumer<String> gameRemovedListener;

  private LobbyWatcherClient lobbyWatcherClient;

  @BeforeEach
  void setUp() {
    lobbyWatcherClient = LobbyWatcherClient.newClient(localhost, HOST);

    final var playerToLobbyConnection =
        new PlayerToLobbyConnection(
            localhost,
            PLAYER,
            error -> {
              throw new AssertionError(error);
            });
    playerToLobbyConnection.addMessageListener(
        LobbyGameUpdatedMessage.TYPE,
        messageContext -> gameUpdatedListener.accept(messageContext.getLobbyGameListing()));
    playerToLobbyConnection.addMessageListener(
        LobbyGameRemovedMessage.TYPE,
        messageContext -> gameRemovedListener.accept(messageContext.getGameId()));
  }

  @Test
  @DisplayName("Post a game, verify listener is notified")
  void verifyPostGame() {
    final String gameId = postGame();

    verify(gameUpdatedListener, timeout(2000L))
        .accept(
            LobbyGameListing.builder()
                .gameId(gameId)
                .lobbyGame(GAME_POSTING_REQUEST.getLobbyGame())
                .build());
  }

  /**
   * Posts a game to the test-only endpoint that bypasses reverse connectivity checks, allowing
   * tests to post games without actually hosting one.
   */
  @SneakyThrows
  private String postGame() {
    final String requestBody = OBJECT_MAPPER.writeValueAsString(GAME_POSTING_REQUEST);
    final URI postUri = localhost.resolve(LobbyWatcherController.TEST_ONLY_GAME_POSTING_PATH);

    final HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder()
            .uri(postUri)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody));

    new AuthenticationHeaders(HOST).createHeaders().forEach(requestBuilder::header);

    final HttpResponse<String> response =
        HttpClient.newHttpClient()
            .send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

    return OBJECT_MAPPER.readValue(response.body(), GamePostingResponse.class).getGameId();
  }

  @Test
  @DisplayName("Post and then remove a game, verify remove listener is notified")
  void removeGame() {
    final String gameId = postGame();
    lobbyWatcherClient.removeGame(gameId);

    verify(gameRemovedListener, timeout(2000L).atLeastOnce()).accept(gameId);
  }

  @Test
  @DisplayName("Post and then update a game, verify update listener is notified")
  void gameUpdated() {
    final String gameId = postGame();
    final LobbyGame updatedGame = TestData.LOBBY_GAME.withComments("new comment");
    lobbyWatcherClient.updateGame(gameId, updatedGame);

    verify(gameUpdatedListener, timeout(2000L))
        .accept(LobbyGameListing.builder().gameId(gameId).lobbyGame(updatedGame).build());
  }
}
