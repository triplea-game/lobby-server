package org.triplea.lobby.server.controllers.lobby;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.Collection;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.http.client.lobby.game.lobby.watcher.GameListingClient;
import org.triplea.http.client.lobby.game.lobby.watcher.LobbyGameListing;
import org.triplea.lobby.server.access.authentication.AuthenticatedUser;
import org.triplea.modules.game.listing.GameListing;

/** Controller with endpoints for posting, getting and removing games. */
@ApplicationScoped
@RolesAllowed(UserRole.HOST)
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("RestResourceMethodInspection")
public class GameListingController {

  @Inject GameListing gameListing;

  /** Returns a listing of the current games. */
  @GET
  @Path(GameListingClient.FETCH_GAMES_PATH)
  @RolesAllowed(UserRole.ANONYMOUS)
  public Collection<LobbyGameListing> fetchGames() {
    return gameListing.getGames();
  }

  /** Moderator action to remove a game. */
  @POST
  @Path(GameListingClient.BOOT_GAME_PATH)
  @RolesAllowed(UserRole.MODERATOR)
  public Response bootGame(@Context final SecurityContext sc, final String gameId) {
    gameListing.bootGame(((AuthenticatedUser) sc.getUserPrincipal()).getUserIdOrThrow(), gameId);
    return Response.ok().build();
  }
}
