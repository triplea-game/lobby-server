package org.triplea.lobby.server.controllers.user.account;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.moderator.player.info.FetchPlayerInfoModule;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.domain.data.PlayerChatId;
import org.triplea.http.client.lobby.moderator.PlayerSummary;
import org.triplea.http.client.lobby.player.PlayerLobbyActionsClient;
import org.triplea.lobby.server.HttpController;
import org.triplea.modules.chat.Chatters;
import org.triplea.modules.game.listing.GameListing;

@ApplicationScoped
@RolesAllowed(UserRole.ANONYMOUS)
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlayerInfoController extends HttpController {

  @Inject Jdbi jdbi;
  @Inject Chatters chatters;
  @Inject GameListing gameListing;

  private FetchPlayerInfoModule fetchPlayerInfoModule;

  @PostConstruct
  void init() {
    fetchPlayerInfoModule = FetchPlayerInfoModule.build(jdbi, chatters, gameListing);
  }

  @POST
  @Path(PlayerLobbyActionsClient.FETCH_PLAYER_INFORMATION)
  public PlayerSummary fetchPlayerInfo(@Context final SecurityContext sc, final String playerId) {
    if (playerId == null) {
      throw new BadRequestException("playerId is null");
    }
    return UserRole.isModerator(user(sc).getUserRole())
        ? fetchPlayerInfoModule.fetchPlayerInfoAsModerator(PlayerChatId.of(playerId))
        : fetchPlayerInfoModule.fetchPlayerInfo(PlayerChatId.of(playerId));
  }
}
