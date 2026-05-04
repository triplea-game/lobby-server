package org.triplea.lobby.server.controllers.lobby;

import com.google.common.base.Preconditions;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Collection;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.http.client.lobby.player.PlayerLobbyActionsClient;
import org.triplea.lobby.server.HttpController;
import org.triplea.modules.game.listing.GameListing;

@ApplicationScoped
@RolesAllowed(UserRole.ANONYMOUS)
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("RestResourceMethodInspection")
public class PlayersInGameController extends HttpController {

  @Inject GameListing gameListing;

  @POST
  @Path(PlayerLobbyActionsClient.FETCH_PLAYERS_IN_GAME)
  public Collection<String> fetchPlayersInGame(final String gameId) {
    Preconditions.checkNotNull(gameId);
    return gameListing.getPlayersInGame(gameId);
  }
}
