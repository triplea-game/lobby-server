package org.triplea.lobby.server.controllers.user.account;

import com.google.common.base.Preconditions;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.domain.data.PlayerChatId;
import org.triplea.http.client.lobby.moderator.PlayerSummary;
import org.triplea.http.client.lobby.player.PlayerLobbyActionsClient;
import org.triplea.lobby.server.HttpController;
import org.triplea.lobby.server.access.authentication.AuthenticatedUser;
import org.triplea.modules.chat.Chatters;
import org.triplea.modules.game.listing.GameListing;
import org.triplea.modules.player.info.FetchPlayerInfoModule;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RolesAllowed(UserRole.ANONYMOUS)
public class PlayerInfoController extends HttpController {

  private final FetchPlayerInfoModule fetchPlayerInfoModule;

  public static PlayerInfoController build(
      final Jdbi jdbi, final Chatters chatters, final GameListing gameListing) {
    return new PlayerInfoController(FetchPlayerInfoModule.build(jdbi, chatters, gameListing));
  }

  @POST
  @Path(PlayerLobbyActionsClient.FETCH_PLAYER_INFORMATION)
  public PlayerSummary fetchPlayerInfo(
      @Auth final AuthenticatedUser authenticatedUser, final String playerId) {
    Preconditions.checkNotNull(playerId);
    return UserRole.isModerator(authenticatedUser.getUserRole())
        ? fetchPlayerInfoModule.fetchPlayerInfoAsModerator(PlayerChatId.of(playerId))
        : fetchPlayerInfoModule.fetchPlayerInfo(PlayerChatId.of(playerId));
  }
}
