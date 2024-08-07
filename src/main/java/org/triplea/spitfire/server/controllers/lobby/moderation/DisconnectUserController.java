package org.triplea.spitfire.server.controllers.lobby.moderation;

import com.google.common.base.Preconditions;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.domain.data.PlayerChatId;
import org.triplea.http.client.lobby.moderator.ModeratorLobbyClient;
import org.triplea.modules.chat.Chatters;
import org.triplea.modules.moderation.disconnect.user.DisconnectUserAction;
import org.triplea.spitfire.server.HttpController;
import org.triplea.spitfire.server.access.authentication.AuthenticatedUser;
import org.triplea.web.socket.WebSocketMessagingBus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RolesAllowed(UserRole.MODERATOR)
public class DisconnectUserController extends HttpController {

  private final DisconnectUserAction disconnectUserAction;

  public static DisconnectUserController build(
      final Jdbi jdbi, final Chatters chatters, final WebSocketMessagingBus playerConnections) {
    return new DisconnectUserController(
        DisconnectUserAction.build(jdbi, chatters, playerConnections));
  }

  @POST
  @Path(ModeratorLobbyClient.DISCONNECT_PLAYER_PATH)
  public Response disconnectPlayer(
      @Auth final AuthenticatedUser authenticatedUser, final String playerIdToBan) {
    Preconditions.checkNotNull(playerIdToBan);

    final boolean removed =
        disconnectUserAction.disconnectPlayer(
            authenticatedUser.getUserIdOrThrow(), PlayerChatId.of(playerIdToBan));
    return Response.status(removed ? 200 : 400).build();
  }
}
