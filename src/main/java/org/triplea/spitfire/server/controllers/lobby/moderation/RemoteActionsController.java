package org.triplea.spitfire.server.controllers.lobby.moderation;

import com.google.common.base.Preconditions;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import javax.annotation.Nonnull;
import lombok.Builder;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.http.client.remote.actions.RemoteActionsClient;
import org.triplea.java.ArgChecker;
import org.triplea.java.IpAddressParser;
import org.triplea.modules.moderation.remote.actions.RemoteActionsModule;
import org.triplea.spitfire.server.HttpController;
import org.triplea.spitfire.server.access.authentication.AuthenticatedUser;
import org.triplea.web.socket.WebSocketMessagingBus;

/**
 * Endpoints for moderators to use to issue remote action commands that affect game-hosts, eg:
 * requesting a server to shutdown.
 */
@Builder
public class RemoteActionsController extends HttpController {
  @Nonnull private final RemoteActionsModule remoteActionsModule;

  public static RemoteActionsController build(
      final Jdbi jdbi, final WebSocketMessagingBus gameMessagingBus) {
    return RemoteActionsController.builder()
        .remoteActionsModule(RemoteActionsModule.build(jdbi, gameMessagingBus))
        .build();
  }

  @POST
  @Path(RemoteActionsClient.SEND_SHUTDOWN_PATH)
  @RolesAllowed(UserRole.MODERATOR)
  public Response sendShutdownSignal(
      @Auth final AuthenticatedUser authenticatedUser, final String gameId) {
    Preconditions.checkNotNull(gameId);

    remoteActionsModule.addGameIdForShutdown(authenticatedUser.getUserIdOrThrow(), gameId);
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
