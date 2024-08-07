package org.triplea.spitfire.server.controllers.lobby.moderation;

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
import org.triplea.http.client.lobby.moderator.toolbox.management.ToolboxModeratorManagementClient;
import org.triplea.modules.moderation.moderators.ModeratorsService;
import org.triplea.spitfire.server.HttpController;
import org.triplea.spitfire.server.access.authentication.AuthenticatedUser;

/**
 * Provides endpoint for moderator maintenance actions and to support the moderators toolbox
 * 'moderators' tab. Actions include: adding moderators, removing moderators, and promoting
 * moderators to 'super-mod'. Some actions are only allowed for super-mods.
 */
@Builder
public class ModeratorsController extends HttpController {
  @Nonnull private final ModeratorsService moderatorsService;

  /** Factory method , instantiates {@code ModeratorsController} with dependencies. */
  public static ModeratorsController build(final Jdbi jdbi) {
    return ModeratorsController.builder() //
        .moderatorsService(ModeratorsService.build(jdbi))
        .build();
  }

  @POST
  @Path(ToolboxModeratorManagementClient.CHECK_USER_EXISTS_PATH)
  @RolesAllowed(UserRole.ADMIN)
  public Response checkUserExists(final String username) {
    return Response.ok().entity(moderatorsService.userExistsByName(username)).build();
  }

  @GET
  @Path(ToolboxModeratorManagementClient.FETCH_MODERATORS_PATH)
  @RolesAllowed(UserRole.MODERATOR)
  public Response getModerators() {
    return Response.ok().entity(moderatorsService.fetchModerators()).build();
  }

  @GET
  @Path(ToolboxModeratorManagementClient.IS_ADMIN_PATH)
  public Response isAdmin(@Auth final AuthenticatedUser authenticatedUser) {
    return Response.ok().entity(authenticatedUser.getUserRole().equals(UserRole.ADMIN)).build();
  }

  @POST
  @Path(ToolboxModeratorManagementClient.REMOVE_MOD_PATH)
  @RolesAllowed(UserRole.ADMIN)
  public Response removeMod(
      @Auth final AuthenticatedUser authenticatedUser, final String moderatorName) {
    moderatorsService.removeMod(authenticatedUser.getUserIdOrThrow(), moderatorName);
    return Response.ok().build();
  }

  @POST
  @Path(ToolboxModeratorManagementClient.ADD_ADMIN_PATH)
  @RolesAllowed(UserRole.ADMIN)
  public Response setAdmin(
      @Auth final AuthenticatedUser authenticatedUser, final String moderatorName) {
    moderatorsService.addAdmin(authenticatedUser.getUserIdOrThrow(), moderatorName);
    return Response.ok().build();
  }

  @POST
  @Path(ToolboxModeratorManagementClient.ADD_MODERATOR_PATH)
  @RolesAllowed(UserRole.ADMIN)
  public Response addModerator(
      @Auth final AuthenticatedUser authenticatedUser, final String username) {
    moderatorsService.addModerator(authenticatedUser.getUserIdOrThrow(), username);
    return Response.ok().build();
  }
}
