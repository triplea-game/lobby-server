package org.triplea.db.dao.lobby.games;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.triplea.db.dao.api.key.ApiKeyHasher;
import org.triplea.domain.data.ApiKey;
import org.triplea.http.client.lobby.game.lobby.watcher.LobbyGameListing;
import org.triplea.java.Postconditions;

/**
 * Game chat history table stores chat messages that have happened in games. This data is upload by
 * game servers to the lobby and is then recorded in database.
 */
public interface LobbyGameDao {

  default void insertLobbyGame(ApiKey apiKey, LobbyGameListing lobbyGameListing) {
    final String hashedkey = new ApiKeyHasher().apply(apiKey);
    final int insertCount =
        insertLobbyGame(
            lobbyGameListing.getLobbyGame().getHostName(), //
            lobbyGameListing.getGameId(),
            hashedkey);
    Postconditions.assertState(
        insertCount == 1, "Failed to insert lobby game: " + lobbyGameListing);
  }

  @SqlUpdate(
      "insert into lobby_game(host_name, game_id, game_hosting_api_key_id) "
          + "values ("
          + "  :hostName,"
          + "  :gameId,"
          + "  (select id from game_hosting_api_key where key = :apiKey))")
  int insertLobbyGame(
      @Bind("hostName") String hostName,
      @Bind("gameId") String gameId,
      @Bind("apiKey") String apiKey);
}
