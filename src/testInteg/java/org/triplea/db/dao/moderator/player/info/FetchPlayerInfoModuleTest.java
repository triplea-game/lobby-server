package org.triplea.db.dao.moderator.player.info;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.net.InetAddresses;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.triplea.db.dao.api.key.PlayerApiKeyDaoWrapper;
import org.triplea.db.dao.api.key.PlayerIdentifiersByApiKeyLookup;
import org.triplea.db.dao.user.history.PlayerHistoryDao;
import org.triplea.db.dao.user.history.PlayerHistoryRecord;
import org.triplea.domain.data.UserName;
import org.triplea.http.client.lobby.moderator.PlayerSummary.Alias;
import org.triplea.http.client.lobby.moderator.PlayerSummary.BanInformation;
import org.triplea.http.client.lobby.web.socket.messages.envelopes.chat.ChatParticipant;
import org.triplea.modules.chat.ChatterSession;
import org.triplea.modules.chat.Chatters;
import org.triplea.modules.game.listing.GameListing;
import org.triplea.web.socket.WebSocketSession;

@ExtendWith(MockitoExtension.class)
class FetchPlayerInfoModuleTest {

  private static final PlayerIdentifiersByApiKeyLookup GAME_PLAYER_LOOKUP =
      PlayerIdentifiersByApiKeyLookup.builder()
          .ip("1.1.1.1")
          .systemId("system-id")
          .userName("user-name")
          .build();

  private static final PlayerAliasRecord PLAYER_ALIAS_RECORD =
      PlayerAliasRecord.builder()
          .username("alias-user-name")
          .systemId("system-id2")
          .ip("2.3.2.3")
          .accessTime(LocalDateTime.of(2000, 1, 1, 1, 1, 1).toInstant(ZoneOffset.UTC))
          .build();

  private static final PlayerBanRecord PLAYER_BAN_RECORD =
      PlayerBanRecord.builder()
          .username("banned-name")
          .systemId("id-at-time-of-ban")
          .ip("5.5.5.6")
          .banStart(LocalDateTime.of(2001, 1, 1, 1, 1, 1).toInstant(ZoneOffset.UTC))
          .banEnd(LocalDateTime.of(2100, 1, 1, 1, 1, 1).toInstant(ZoneOffset.UTC))
          .build();

  @Mock private PlayerApiKeyDaoWrapper apiKeyDaoWrapper;
  @Mock private PlayerInfoForModeratorDao playerInfoForModeratorDao;
  @Mock private PlayerHistoryDao playerHistoryDao;
  @Mock private Chatters chatters;
  @Mock private GameListing gameListing;

  @InjectMocks private FetchPlayerInfoModule fetchPlayerInfoModule;

  private ChatterSession chatterSession;

  @BeforeEach
  void setupChatterSessionData() {
    chatterSession =
        ChatterSession.builder()
            .ip(InetAddresses.forString("1.2.3.4"))
            .apiKeyId(-1)
            .chatParticipant(
                ChatParticipant.builder()
                    .userName("user-name")
                    .isModerator(false)
                    .status("AFK")
                    .playerChatId("player-chat-id")
                    .build())
            .session(mock(WebSocketSession.class))
            .build();
  }

  @Test
  void unableToFindPlayerChatIdInChattersThrows() {
    when(chatters.lookupPlayerByChatId("id")).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> fetchPlayerInfoModule.fetchPlayerInfo("id"));
  }

  @Test
  void unableToFindPlayerChatIdInApiKeyTableThrows() {
    when(chatters.lookupPlayerByChatId("id")).thenReturn(Optional.of(chatterSession));
    when(apiKeyDaoWrapper.lookupPlayerByChatId("id")) //
        .thenReturn(Optional.empty());
    assertThrows(
        IllegalArgumentException.class,
        () -> fetchPlayerInfoModule.fetchPlayerInfoAsModerator("id"),
        "Using the lookup play info function requires a player to be in the lobby, "
            + "we therefore expect to find their play id, or else throws.");
  }

  @Test
  @DisplayName("Verify data transformation retrieving info available to any player")
  void playerLookupByPlayer() {
    when(chatters.lookupPlayerByChatId("id")).thenReturn(Optional.of(chatterSession));

    when(gameListing.getGameNamesPlayerHasJoined(
            UserName.of(chatterSession.getChatParticipant().getUserName())))
        .thenReturn(Set.of("Host1", "Host2"));

    final var playerSummaryForPlayer = fetchPlayerInfoModule.fetchPlayerInfo("id");

    assertThat(playerSummaryForPlayer.getCurrentGames(), hasItems("Host1", "Host2"));

    // lookup of more information is reserved to moderator players.
    verify(apiKeyDaoWrapper, never()).lookupPlayerByChatId(any());
  }

  @Test
  void lookupRegistrationDate() {
    givenChatIdToUserIdLookup("chat-id", 123);
    when(playerHistoryDao.lookupPlayerHistoryByUserId(123))
        .thenReturn(Optional.of(new PlayerHistoryRecord(Instant.ofEpochMilli(5000))));

    final var playerSummaryForPlayer = fetchPlayerInfoModule.fetchPlayerInfo("chat-id");

    assertThat(playerSummaryForPlayer.getRegistrationDateEpochMillis(), is(5000L));
  }

  private void givenChatIdToUserIdLookup(final String playerChatId, final Integer userId) {
    when(chatters.lookupPlayerByChatId(playerChatId)).thenReturn(Optional.of(chatterSession));
    when(apiKeyDaoWrapper.lookupUserIdByChatId("chat-id")).thenReturn(Optional.ofNullable(userId));
  }

  @Test
  void lookupRegistrationDateNotRegisteredUserCase() {
    givenChatIdToUserIdLookup("chat-id", null);

    final var playerSummaryForPlayer = fetchPlayerInfoModule.fetchPlayerInfo("chat-id");

    assertThat(playerSummaryForPlayer.getRegistrationDateEpochMillis(), is(nullValue()));
  }

  @Test
  @DisplayName("Verify data transformation into a player summary object")
  void playerLookupByModerator() {
    when(chatters.lookupPlayerByChatId("id")).thenReturn(Optional.of(chatterSession));
    when(apiKeyDaoWrapper.lookupPlayerByChatId("id")).thenReturn(Optional.of(GAME_PLAYER_LOOKUP));
    when(playerInfoForModeratorDao.lookupPlayerAliasRecords(
            GAME_PLAYER_LOOKUP.getSystemId().getValue(), GAME_PLAYER_LOOKUP.getIp()))
        .thenReturn(List.of(PLAYER_ALIAS_RECORD));
    when(playerInfoForModeratorDao.lookupPlayerBanRecords(
            GAME_PLAYER_LOOKUP.getSystemId().getValue(), GAME_PLAYER_LOOKUP.getIp()))
        .thenReturn(List.of(PLAYER_BAN_RECORD));

    final var playerSummaryForModerator = fetchPlayerInfoModule.fetchPlayerInfoAsModerator("id");
    assertThat(playerSummaryForModerator.getIp(), is(chatterSession.getIp().toString()));
    assertThat(
        playerSummaryForModerator.getSystemId(), is(GAME_PLAYER_LOOKUP.getSystemId().getValue()));

    assertThat(playerSummaryForModerator.getBans(), hasSize(1));
    assertThat(
        playerSummaryForModerator.getBans().iterator().next(),
        is(
            BanInformation.builder()
                .ip(PLAYER_BAN_RECORD.getIp())
                .systemId(PLAYER_BAN_RECORD.getSystemId())
                .name(PLAYER_BAN_RECORD.getUsername())
                .epochMilliStartDate(PLAYER_BAN_RECORD.getBanStart().toEpochMilli())
                .epochMillEndDate(PLAYER_BAN_RECORD.getBanEnd().toEpochMilli())
                .build()));

    assertThat(playerSummaryForModerator.getAliases(), hasSize(1));
    assertThat(
        playerSummaryForModerator.getAliases().iterator().next(),
        is(
            Alias.builder()
                .systemId(PLAYER_ALIAS_RECORD.getSystemId())
                .name(PLAYER_ALIAS_RECORD.getUsername())
                .ip(PLAYER_ALIAS_RECORD.getIp())
                .epochMilliDate(PLAYER_ALIAS_RECORD.getDate().toEpochMilli())
                .build()));
  }
}
