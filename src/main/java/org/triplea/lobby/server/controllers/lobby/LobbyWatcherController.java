package org.triplea.lobby.server.controllers.lobby;

import com.google.common.base.Preconditions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.domain.data.UserName;
import org.triplea.http.client.lobby.game.lobby.watcher.GamePostingRequest;
import org.triplea.http.client.lobby.game.lobby.watcher.GamePostingResponse;
import org.triplea.http.client.lobby.game.lobby.watcher.LobbyWatcherClient;
import org.triplea.http.client.lobby.game.lobby.watcher.PlayerJoinedNotification;
import org.triplea.http.client.lobby.game.lobby.watcher.PlayerLeftNotification;
import org.triplea.http.client.lobby.game.lobby.watcher.UpdateGameRequest;
import org.triplea.lobby.server.HttpController;
import org.triplea.modules.game.listing.GameListing;
import org.triplea.modules.game.lobby.watcher.GamePostingModule;

/** Controller with endpoints for posting, updating and removing games from the lobby listing. */
@ApplicationScoped
@RolesAllowed(UserRole.HOST)
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("RestResourceMethodInspection")
public class LobbyWatcherController extends HttpController {

  public static final String TEST_ONLY_GAME_POSTING_PATH = "/test-only/lobby/post-game";

  @ConfigProperty(name = "app.game-host-connectivity-check-enabled", defaultValue = "false")
  boolean gameHostConnectivityCheckEnabled;

  @Inject GameListing gameListing;

  private GamePostingModule gamePostingModule;

  @PostConstruct
  void init() {
    gamePostingModule = GamePostingModule.build(gameListing);
  }

  @POST
  @Path(LobbyWatcherClient.POST_GAME_PATH)
  public GamePostingResponse postGame(
      @Context final SecurityContext sc, final GamePostingRequest gamePostingRequest) {
    Preconditions.checkArgument(gamePostingRequest != null);
    Preconditions.checkArgument(gamePostingRequest.getLobbyGame() != null);
    return gamePostingModule.postGame(user(sc).getApiKey(), gamePostingRequest);
  }

  /**
   * Available only when the connectivity check is disabled (non-prod). Lets integration tests post
   * a game without actually hosting one, bypassing the reverse-connectivity check.
   */
  @POST
  @Path(TEST_ONLY_GAME_POSTING_PATH)
  public Response postGameTestOnly(
      @Context final SecurityContext sc, final GamePostingRequest gamePostingRequest) {
    Preconditions.checkArgument(gamePostingRequest != null);
    Preconditions.checkArgument(gamePostingRequest.getLobbyGame() != null);

    if (gameHostConnectivityCheckEnabled) {
      return Response.status(404).build();
    }
    return Response.ok()
        .entity(
            GamePostingResponse.builder()
                .connectivityCheckSucceeded(true)
                .gameId(gameListing.postGame(user(sc).getApiKey(), gamePostingRequest))
                .build())
        .build();
  }

  @POST
  @Path(LobbyWatcherClient.REMOVE_GAME_PATH)
  public Response removeGame(@Context final SecurityContext sc, final String gameId) {
    gameListing.removeGame(user(sc).getApiKey(), gameId);
    return Response.ok().build();
  }

  /**
   * Heartbeat endpoint. Returns {@code true} if the game is still listed; {@code false} if it was
   * already removed and the host should re-post.
   */
  @POST
  @Path(LobbyWatcherClient.KEEP_ALIVE_PATH)
  public boolean keepAlive(@Context final SecurityContext sc, final String gameId) {
    return gameListing.keepAlive(user(sc).getApiKey(), gameId);
  }

  @POST
  @Path(LobbyWatcherClient.UPDATE_GAME_PATH)
  public Response updateGame(
      @Context final SecurityContext sc, final UpdateGameRequest updateGameRequest) {
    gameListing.updateGame(
        user(sc).getApiKey(), updateGameRequest.getGameId(), updateGameRequest.getGameData());
    return Response.ok().build();
  }

  @POST
  @Path(LobbyWatcherClient.PLAYER_JOINED_PATH)
  public Response playerJoinedGame(
      @Context final SecurityContext sc, final PlayerJoinedNotification playerJoinedNotification) {
    gameListing.addPlayerToGame(
        UserName.of(playerJoinedNotification.getPlayerName()),
        user(sc).getApiKey(),
        playerJoinedNotification.getGameId());
    return Response.ok().build();
  }

  @POST
  @Path(LobbyWatcherClient.PLAYER_LEFT_PATH)
  public Response playerLeftGame(
      @Context final SecurityContext sc, final PlayerLeftNotification playerLeftNotification) {
    gameListing.removePlayerFromGame(
        UserName.of(playerLeftNotification.getPlayerName()),
        user(sc).getApiKey(),
        playerLeftNotification.getGameId());

    return Response.ok().build();
  }
}
