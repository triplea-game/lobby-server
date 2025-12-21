package org.triplea.lobby.server.controllers.lobby;

import com.google.common.base.Preconditions;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.util.Collection;
import lombok.AllArgsConstructor;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.http.client.lobby.player.PlayerLobbyActionsClient;
import org.triplea.lobby.server.HttpController;
import org.triplea.lobby.server.access.authentication.AuthenticatedUser;
import org.triplea.modules.game.listing.GameListing;

@RolesAllowed(UserRole.ANONYMOUS)
@AllArgsConstructor
public class PlayersInGameController extends HttpController {
  private final GameListing gameListing;

  public static PlayersInGameController build(final GameListing gameListing) {
    return new PlayersInGameController(gameListing);
  }

  @POST
  @Path(PlayerLobbyActionsClient.FETCH_PLAYERS_IN_GAME)
  public Collection<String> fetchPlayersInGame(
      @Auth final AuthenticatedUser authenticatedUser, final String gameId) {
    Preconditions.checkNotNull(gameId);
    return gameListing.getPlayersInGame(gameId);
  }
}
