package org.triplea.modules.game.listing;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.collection.IsMapWithSize.anEmptyMap;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.triplea.db.dao.lobby.games.LobbyGameDao;
import org.triplea.db.dao.moderator.ModeratorAuditHistoryDao;
import org.triplea.domain.data.ApiKey;
import org.triplea.domain.data.LobbyGame;
import org.triplea.domain.data.UserName;
import org.triplea.http.client.lobby.game.lobby.watcher.GamePostingRequest;
import org.triplea.http.client.lobby.game.lobby.watcher.LobbyGameListing;
import org.triplea.http.client.web.socket.MessageEnvelope;
import org.triplea.http.client.web.socket.messages.envelopes.game.listing.LobbyGameRemovedMessage;
import org.triplea.http.client.web.socket.messages.envelopes.game.listing.LobbyGameUpdatedMessage;
import org.triplea.java.IpAddressParser;
import org.triplea.java.cache.ttl.ExpiringAfterWriteTtlCache;
import org.triplea.web.socket.WebSocketMessagingBus;

/**
 * Items to test.: <br>
 * - core functionality of get, add, remove, and keep-alive<br>
 * - games are added with an API key, keep-alive and remove should not do anything if API key is
 * incorrect<br>
 * - 'dead-games' should be reaped on 'get'<br>
 * - boot-game, the authentication should be already done on controller level, so we just need to be
 * sure there is bookkeeping.
 */
@ExtendWith(MockitoExtension.class)
class GameListingTest {
  private static final String GAME_ID_0 = "id0";
  private static final String GAME_ID_1 = "id1";
  private static final String GAME_ID_2 = "id2";

  private static final ApiKey API_KEY_0 = ApiKey.of("apiKey0");
  private static final ApiKey API_KEY_1 = ApiKey.of("apiKey1");

  private static final GameListing.GameId ID_0 = new GameListing.GameId(API_KEY_0, GAME_ID_0);
  private static final GameListing.GameId ID_1 = new GameListing.GameId(API_KEY_1, GAME_ID_1);

  private static final String HOST_NAME = "host-player";
  private static final int MODERATOR_ID = 33;

  private final ExpiringAfterWriteTtlCache<GameListing.GameId, LobbyGame> cache =
      new ExpiringAfterWriteTtlCache<>(1, TimeUnit.HOURS, (key, value) -> {});

  @Mock private ModeratorAuditHistoryDao moderatorAuditHistoryDao;
  @Mock private LobbyGameDao lobbyGameDao;
  @Mock private WebSocketMessagingBus playerMessagingBus;

  private GameListing gameListing;

  @Mock private LobbyGame lobbyGame0;
  @Mock private LobbyGame lobbyGame1;
  @Mock private LobbyGame lobbyGame2;

  @BeforeEach
  void setUp() {
    gameListing =
        GameListing.builder()
            .playerMessagingBus(playerMessagingBus)
            .auditHistoryDao(moderatorAuditHistoryDao)
            .lobbyGameDao(lobbyGameDao)
            .games(cache)
            .build();
  }

  @Nested
  final class GetGames {

    /** Basic case, no games added, expect none to be returned. */
    @Test
    void getGames() {
      cache.put(ID_0, lobbyGame0);
      cache.put(new GameListing.GameId(API_KEY_0, GAME_ID_1), lobbyGame1);
      cache.put(new GameListing.GameId(API_KEY_1, GAME_ID_2), lobbyGame2);

      final List<LobbyGameListing> result = gameListing.getGames();

      assertThat(
          result,
          hasItem(LobbyGameListing.builder().gameId(GAME_ID_0).lobbyGame(lobbyGame0).build()));

      assertThat(
          result,
          hasItem(LobbyGameListing.builder().gameId(GAME_ID_1).lobbyGame(lobbyGame1).build()));

      assertThat(
          result,
          hasItem(LobbyGameListing.builder().gameId(GAME_ID_2).lobbyGame(lobbyGame2).build()));
    }
  }

  @Nested
  final class KeepAlive {
    @Test
    void noGamesPresent() {
      final boolean result = gameListing.keepAlive(API_KEY_0, GAME_ID_0);
      assertThat("Game not found, keep alive should return false", result, is(false));

      assertThat(cache.asMap(), is(anEmptyMap()));
    }

    @Test
    void gameExists() {
      cache.put(ID_0, lobbyGame0);

      final boolean result = gameListing.keepAlive(API_KEY_0, GAME_ID_0);

      assertThat("Game found, keep alive should return true", result, is(true));
      assertThat(cache.asMap(), is(aMapWithSize(1)));
      assertThat(cache.asMap(), hasEntry(ID_0, lobbyGame0));
    }
  }

  @Nested
  final class RemoveGame {
    @Test
    void removeGame() {
      cache.put(new GameListing.GameId(API_KEY_0, GAME_ID_0), lobbyGame0);

      gameListing.removeGame(API_KEY_0, GAME_ID_0);

      assertThat(cache.asMap(), is(anEmptyMap()));
      verify(playerMessagingBus).broadcastMessage(new LobbyGameRemovedMessage(GAME_ID_0));
    }

    @Test
    void removeGameRequiresCorrectApiKey() {
      cache.put(new GameListing.GameId(API_KEY_0, GAME_ID_0), lobbyGame0);

      gameListing.removeGame(API_KEY_1, GAME_ID_0);

      assertThat(cache.asMap(), is(aMapWithSize(1)));
      assertThat(cache.asMap(), hasEntry(new GameListing.GameId(API_KEY_0, GAME_ID_0), lobbyGame0));
      verify(playerMessagingBus, never()).broadcastMessage(any(MessageEnvelope.class));
    }
  }

  @Nested
  final class PostGame {
    @Test
    void postGame() {
      final String id0 =
          gameListing.postGame(
              API_KEY_0,
              GamePostingRequest.builder().lobbyGame(lobbyGame0).playerNames(List.of()).build());

      assertThat(id0, not(emptyString()));
      assertThat(cache.asMap(), is(Map.of(new GameListing.GameId(API_KEY_0, id0), lobbyGame0)));

      final var lobbyGameListing =
          LobbyGameListing.builder().gameId(id0).lobbyGame(lobbyGame0).build();
      verify(playerMessagingBus).broadcastMessage(new LobbyGameUpdatedMessage(lobbyGameListing));
      verify(lobbyGameDao).insertLobbyGame(API_KEY_0, lobbyGameListing);
    }
  }

  @Nested
  final class UpdateGame {
    @Test
    void updateGameThatDoesNotExist() {
      final boolean result = gameListing.updateGame(API_KEY_0, GAME_ID_0, lobbyGame0);

      assertThat(result, is(false));
      assertThat(cache.asMap(), is(anEmptyMap()));
      verify(playerMessagingBus, never()).broadcastMessage(any(MessageEnvelope.class));
    }

    @Test
    void updateGameThatDoesExist() {
      cache.put(ID_0, lobbyGame1);

      final boolean result = gameListing.updateGame(API_KEY_0, GAME_ID_0, lobbyGame0);

      assertThat(result, is(true));

      verify(playerMessagingBus)
          .broadcastMessage(
              new LobbyGameUpdatedMessage(
                  LobbyGameListing.builder().gameId(GAME_ID_0).lobbyGame(lobbyGame0).build()));
    }
  }

  @Nested
  final class BootGame {
    @Test
    void bootGame() {
      cache.put(ID_0, lobbyGame0);
      when(lobbyGame0.getHostName()).thenReturn(HOST_NAME);

      gameListing.bootGame(MODERATOR_ID, GAME_ID_0);

      assertThat(cache.asMap(), is(anEmptyMap()));
      verify(moderatorAuditHistoryDao)
          .addAuditRecord(
              ModeratorAuditHistoryDao.AuditArgs.builder()
                  .actionName(ModeratorAuditHistoryDao.AuditAction.BOOT_GAME)
                  .actionTarget(HOST_NAME)
                  .moderatorUserId(MODERATOR_ID)
                  .build());
      verify(playerMessagingBus).broadcastMessage(new LobbyGameRemovedMessage(GAME_ID_0));
    }
  }

  @Nested
  final class IsValidGameIdApiKeyPair {

    @Test
    void validCase() {
      cache.put(ID_0, lobbyGame0);
      final boolean result = gameListing.isValidApiKeyAndGameId(ID_0.getApiKey(), ID_0.getId());
      assertThat(result, is(true));
    }

    @Test
    void mismatchOnApiKey() {
      cache.put(ID_0, lobbyGame0);
      final boolean result =
          gameListing.isValidApiKeyAndGameId(ApiKey.of("incorrect-api-key"), ID_0.getId());
      assertThat(result, is(false));
    }

    @Test
    void mismatchOnGameId() {
      cache.put(ID_0, lobbyGame0);
      final boolean result =
          gameListing.isValidApiKeyAndGameId(ID_0.getApiKey(), "incorrect-game-id");
      assertThat(result, is(false));
    }
  }

  @Nested
  class GetHostForGame {
    @Test
    void doesNotExistCase() {
      final Optional<InetSocketAddress> result =
          gameListing.getHostForGame(ID_0.getApiKey(), "incorrect-game-id");

      assertThat(result, isEmpty());
    }

    @Test
    void badApiKeyCase() {
      cache.put(ID_0, lobbyGame0);

      final Optional<InetSocketAddress> result =
          gameListing.getHostForGame(ApiKey.of("incorrect"), ID_0.getId());

      assertThat(result, isEmpty());
    }

    @Test
    void badGameId() {
      cache.put(ID_0, lobbyGame0);

      final Optional<InetSocketAddress> result =
          gameListing.getHostForGame(ID_0.getApiKey(), "bad-game-id");

      assertThat(result, isEmpty());
    }

    @Test
    void happyCaseFindByApiKeyAndGameId() {
      when(lobbyGame0.getHostAddress()).thenReturn("1.1.1.1");
      cache.put(ID_0, lobbyGame0);

      final Optional<InetSocketAddress> result =
          gameListing.getHostForGame(ID_0.getApiKey(), ID_0.getId());

      assertThat(
          result,
          isPresentAndIs(
              new InetSocketAddress(
                  IpAddressParser.fromString("1.1.1.1"), lobbyGame0.getHostPort())));
    }
  }

  @Nested
  class PlayerIsInGameTracking {
    @Test
    void playerIsNotInAnyGamesEmptyCase() {
      final UserName user = UserName.of("user");

      final Collection<String> results = gameListing.getGameNamesPlayerHasJoined(user);

      assertThat(results, is(empty()));
    }

    @Test
    void addPlayerToSingleGame() {
      final UserName user = UserName.of("user");
      cache.put(ID_0, lobbyGame0);
      gameListing.addPlayerToGame(user, ID_0.getApiKey(), ID_0.getId());

      final Collection<String> results = gameListing.getGameNamesPlayerHasJoined(user);

      assertThat(results, hasSize(1));
    }

    @Test
    void addPlayerToSingleGameAndThenRemove() {
      final UserName user = UserName.of("user");
      cache.put(ID_0, lobbyGame0);
      gameListing.addPlayerToGame(user, ID_0.getApiKey(), ID_0.getId());
      gameListing.removePlayerFromGame(user, ID_0.getApiKey(), ID_0.getId());

      final Collection<String> results = gameListing.getGameNamesPlayerHasJoined(user);

      assertThat(
          "player was added and then remove from a game, expect to no longer be in a game",
          results,
          is(empty()));
    }

    @Test
    void addPlayerToMultipleGame() {
      final UserName user = UserName.of("user");
      cache.put(ID_0, lobbyGame0);
      cache.put(ID_1, lobbyGame1);
      gameListing.addPlayerToGame(user, ID_0.getApiKey(), ID_0.getId());
      gameListing.addPlayerToGame(user, ID_1.getApiKey(), ID_1.getId());

      final Collection<String> results = gameListing.getGameNamesPlayerHasJoined(user);

      assertThat(results, hasSize(2));
      assertThat(results, hasItem(lobbyGame0.getHostName()));
      assertThat(results, hasItem(lobbyGame1.getHostName()));
    }

    @Test
    void addPlayerToMultipleGameAndRemoveFromOneGame() {
      final UserName user = UserName.of("user");
      cache.put(ID_0, lobbyGame0);
      cache.put(ID_1, lobbyGame1);
      gameListing.addPlayerToGame(user, ID_0.getApiKey(), ID_0.getId());
      gameListing.addPlayerToGame(user, ID_1.getApiKey(), ID_1.getId());
      gameListing.removePlayerFromGame(user, ID_0.getApiKey(), ID_0.getId());

      final Collection<String> results = gameListing.getGameNamesPlayerHasJoined(user);

      assertThat(results, hasSize(1));
      assertThat(
          "Player was removed from game0, should still be in game1",
          results,
          hasItem(lobbyGame1.getHostName()));
    }

    @Test
    @DisplayName(
        "Games can expire, our tracking of player to games should take "
            + "into account expired games")
    void getPlayerInGamesOnlyReturnsCurrentGames() {
      final UserName user = UserName.of("user");
      cache.put(ID_0, lobbyGame0);
      gameListing.addPlayerToGame(user, ID_0.getApiKey(), ID_0.getId());
      gameListing.addPlayerToGame(user, ID_1.getApiKey(), ID_1.getId());

      final Collection<String> results = gameListing.getGameNamesPlayerHasJoined(user);

      assertThat("Player was added to two games, but only one game is alive", results, hasSize(1));
      assertThat(results, hasItem(lobbyGame0.getHostName()));
    }

    @Test
    void removingGamesWillExplicityRemoveParticipants() {
      final UserName user = UserName.of("user");
      cache.put(ID_0, lobbyGame0);
      gameListing.addPlayerToGame(user, ID_0.getApiKey(), ID_0.getId());
      gameListing.removeGame(ID_0.getApiKey(), ID_0.getId());

      final Collection<String> results = gameListing.getGameNamesPlayerHasJoined(user);

      assertThat(
          "Player was in one game, but it was removed, should be empty", results, is(empty()));
    }

    @Test
    void postingGameAddsPlayersFromThatGame() {
      gameListing.postGame(
          API_KEY_0,
          GamePostingRequest.builder()
              .lobbyGame(lobbyGame0)
              .playerNames(List.of("player1", "player2"))
              .build());

      Collection<String> results = gameListing.getGameNamesPlayerHasJoined(UserName.of("player1"));

      assertThat(results, hasSize(1));
      assertThat(results, hasItem(lobbyGame0.getHostName()));

      // verify player2 was added
      results = gameListing.getGameNamesPlayerHasJoined(UserName.of("player2"));

      assertThat(results, hasSize(1));
      assertThat(results, hasItem(lobbyGame0.getHostName()));
    }
  }

  @Nested
  class PlayersInGame {
    @Test
    void emptyCase() {
      assertThat(gameListing.getPlayersInGame("game-id"), is(empty()));
    }

    @Test
    void wrongGameCase() {
      cache.put(ID_0, lobbyGame0);

      assertThat(gameListing.getPlayersInGame("DNE"), is(empty()));
    }

    @Test
    void hasPlayers() {
      cache.put(ID_0, lobbyGame0);

      gameListing.addPlayerToGame(UserName.of("player1"), ID_0.getApiKey(), ID_0.getId());
      gameListing.addPlayerToGame(UserName.of("player2"), ID_0.getApiKey(), ID_0.getId());

      assertThat(gameListing.getPlayersInGame(ID_0.getId()), hasSize(2));
      assertThat(gameListing.getPlayersInGame(ID_0.getId()), hasItems("player1", "player2"));
    }
  }
}
