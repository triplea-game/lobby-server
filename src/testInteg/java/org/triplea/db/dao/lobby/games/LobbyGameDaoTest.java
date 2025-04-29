package org.triplea.db.dao.lobby.games;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.triplea.IntegTestExtension;
import org.triplea.TestData;
import org.triplea.domain.data.ApiKey;
import org.triplea.http.client.lobby.game.lobby.watcher.LobbyGameListing;

@RequiredArgsConstructor
@ExtendWith(IntegTestExtension.class)
@ExtendWith(DBUnitExtension.class)
class LobbyGameDaoTest {
  private final LobbyGameDao lobbyGameDao;

  @Test
  @DataSet(value = "lobby_games/game_hosting_api_key.yml", useSequenceFiltering = false)
  @ExpectedDataSet("lobby_games/lobby_game_post_insert.yml")
  void insertLobbyGame() {
    lobbyGameDao.insertLobbyGame(
        ApiKey.of("HOST"),
        LobbyGameListing.builder() //
            .gameId("game-id")
            .lobbyGame(TestData.LOBBY_GAME)
            .build());
  }
}
