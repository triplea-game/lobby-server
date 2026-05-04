package org.triplea.lobby.server.controllers.lobby.moderation;

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
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.domain.data.PlayerChatId;
import org.triplea.http.client.lobby.moderator.ModeratorLobbyClient;
import org.triplea.lobby.server.HttpController;
import org.triplea.modules.chat.Chatters;
import org.triplea.modules.moderation.disconnect.user.DisconnectUserAction;
import org.triplea.server.qualifier.PlayerConnections;
import org.triplea.web.socket.WebSocketMessagingBus;

@ApplicationScoped
@RolesAllowed(UserRole.MODERATOR)
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DisconnectUserController extends HttpController {

  @Inject Jdbi jdbi;
  @Inject Chatters chatters;

  @Inject @PlayerConnections WebSocketMessagingBus playerConnections;

  private DisconnectUserAction disconnectUserAction;

  @PostConstruct
  void init() {
    disconnectUserAction = DisconnectUserAction.build(jdbi, chatters, playerConnections);
  }

  @POST
  @Path(ModeratorLobbyClient.DISCONNECT_PLAYER_PATH)
  public Response disconnectPlayer(@Context final SecurityContext sc, final String playerIdToBan) {
    Preconditions.checkNotNull(playerIdToBan);
    final boolean removed =
        disconnectUserAction.disconnectPlayer(
            user(sc).getUserIdOrThrow(), PlayerChatId.of(playerIdToBan));
    return Response.status(removed ? 200 : 400).build();
  }
}
