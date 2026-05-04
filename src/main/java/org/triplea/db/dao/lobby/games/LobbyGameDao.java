package org.triplea.db.dao.lobby.games;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.api.key.ApiKeyHasher;
import org.triplea.domain.data.ApiKey;
import org.triplea.http.client.lobby.game.lobby.watcher.LobbyGameListing;
import org.triplea.java.Postconditions;

/**
 * Game chat history table stores chat messages that have happened in games. This data is upload by
 * game servers to the lobby and is then recorded in database.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class LobbyGameDao {
  private final Jdbi jdbi;

  public void insertLobbyGame(ApiKey apiKey, LobbyGameListing lobbyGameListing) {
    final String hashedkey = new ApiKeyHasher().apply(apiKey);
    int insertCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createUpdate(
                        "insert into lobby_game(host_name, game_id, game_hosting_api_key_id) "
                            + "values ("
                            + "  :hostName,"
                            + "  :gameId,"
                            + "  (select id from game_hosting_api_key where key = :apiKey))")
                    .bind("hostName", lobbyGameListing.getLobbyGame().getHostName())
                    .bind("gameId", lobbyGameListing.getGameId())
                    .bind("apiKey", hashedkey)
                    .execute());
    Postconditions.assertState(
        insertCount == 1, "Failed to insert lobby game: " + lobbyGameListing);
  }
}
