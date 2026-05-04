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
import org.triplea.http.client.remote.actions.RemoteActionsClient;
import org.triplea.java.ArgChecker;
import org.triplea.java.IpAddressParser;
import org.triplea.lobby.server.HttpController;
import org.triplea.modules.moderation.remote.actions.RemoteActionsModule;
import org.triplea.server.qualifier.GameConnections;
import org.triplea.web.socket.WebSocketMessagingBus;

/**
 * Endpoints for moderators to use to issue remote action commands that affect game-hosts, eg:
 * requesting a server to shutdown.
 */
@ApplicationScoped
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RemoteActionsController extends HttpController {

  @Inject Jdbi jdbi;

  @Inject @GameConnections WebSocketMessagingBus gameMessagingBus;

  private RemoteActionsModule remoteActionsModule;

  @PostConstruct
  void init() {
    remoteActionsModule = RemoteActionsModule.build(jdbi, gameMessagingBus);
  }

  @POST
  @Path(RemoteActionsClient.SEND_SHUTDOWN_PATH)
  @RolesAllowed(UserRole.MODERATOR)
  public Response sendShutdownSignal(@Context final SecurityContext sc, final String gameId) {
    Preconditions.checkNotNull(gameId);
    remoteActionsModule.addGameIdForShutdown(user(sc).getUserIdOrThrow(), gameId);
    return Response.ok().build();
  }

  @POST
  @Path(RemoteActionsClient.IS_PLAYER_BANNED_PATH)
  @RolesAllowed(UserRole.HOST)
  public Response isUserBanned(final String ipAddress) {
    ArgChecker.checkNotEmpty(ipAddress);
    Preconditions.checkArgument(IpAddressParser.isValid(ipAddress));
    final boolean result = remoteActionsModule.isUserBanned(IpAddressParser.fromString(ipAddress));
    return Response.ok().entity(result).build();
  }
}
