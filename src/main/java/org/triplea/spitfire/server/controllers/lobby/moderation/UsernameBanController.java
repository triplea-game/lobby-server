package org.triplea.spitfire.server.controllers.lobby.moderation;

import com.google.common.base.Preconditions;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import javax.annotation.Nonnull;
import lombok.Builder;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.http.client.lobby.moderator.toolbox.banned.name.ToolboxUsernameBanClient;
import org.triplea.modules.moderation.ban.name.UsernameBanService;
import org.triplea.spitfire.server.HttpController;
import org.triplea.spitfire.server.access.authentication.AuthenticatedUser;

/** Endpoint for use by moderators to view, add and remove player username bans. */
@Builder
@RolesAllowed(UserRole.MODERATOR)
public class UsernameBanController extends HttpController {
  @Nonnull private final UsernameBanService bannedNamesService;

  public static UsernameBanController build(final Jdbi jdbi) {
    return UsernameBanController.builder()
        .bannedNamesService(UsernameBanService.build(jdbi))
        .build();
  }

  @POST
  @Path(ToolboxUsernameBanClient.REMOVE_BANNED_USER_NAME_PATH)
  public Response removeBannedUsername(
      @Auth final AuthenticatedUser authenticatedUser, final String username) {
    Preconditions.checkArgument(username != null && !username.isEmpty());
    return Response.status(
            bannedNamesService.removeUsernameBan(authenticatedUser.getUserIdOrThrow(), username)
                ? 200
                : 400)
        .build();
  }

  @POST
  @Path(ToolboxUsernameBanClient.ADD_BANNED_USER_NAME_PATH)
  public Response addBannedUsername(
      @Auth final AuthenticatedUser authenticatedUser, final String username) {
    Preconditions.checkArgument(username != null && !username.isEmpty());
    return Response.status(
            bannedNamesService.addBannedUserName(
                    authenticatedUser.getUserIdOrThrow(), username.toUpperCase())
                ? 200
                : 400)
        .build();
  }

  @GET
  @Path(ToolboxUsernameBanClient.GET_BANNED_USER_NAMES_PATH)
  public Response getBannedUsernames() {
    return Response.status(200).entity(bannedNamesService.getBannedUserNames()).build();
  }
}
