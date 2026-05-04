package org.triplea.lobby.server.controllers.lobby.moderation;

import jakarta.annotation.PostConstruct;
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
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.http.client.lobby.moderator.toolbox.management.ToolboxModeratorManagementClient;
import org.triplea.lobby.server.HttpController;
import org.triplea.modules.moderation.moderators.ModeratorsService;

/**
 * Provides endpoint for moderator maintenance actions and to support the moderators toolbox
 * 'moderators' tab. Actions include: adding moderators, removing moderators, and promoting
 * moderators to 'super-mod'. Some actions are only allowed for super-mods.
 */
@ApplicationScoped
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ModeratorsController extends HttpController {

  @Inject Jdbi jdbi;

  private ModeratorsService moderatorsService;

  @PostConstruct
  void init() {
    moderatorsService = ModeratorsService.build(jdbi);
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
  public Response isAdmin(@Context final SecurityContext sc) {
    return Response.ok().entity(user(sc).getUserRole().equals(UserRole.ADMIN)).build();
  }

  @POST
  @Path(ToolboxModeratorManagementClient.REMOVE_MOD_PATH)
  @RolesAllowed(UserRole.ADMIN)
  public Response removeMod(@Context final SecurityContext sc, final String moderatorName) {
    moderatorsService.removeMod(user(sc).getUserIdOrThrow(), moderatorName);
    return Response.ok().build();
  }

  @POST
  @Path(ToolboxModeratorManagementClient.ADD_ADMIN_PATH)
  @RolesAllowed(UserRole.ADMIN)
  public Response setAdmin(@Context final SecurityContext sc, final String moderatorName) {
    moderatorsService.addAdmin(user(sc).getUserIdOrThrow(), moderatorName);
    return Response.ok().build();
  }

  @POST
  @Path(ToolboxModeratorManagementClient.ADD_MODERATOR_PATH)
  @RolesAllowed(UserRole.ADMIN)
  public Response addModerator(@Context final SecurityContext sc, final String username) {
    moderatorsService.addModerator(user(sc).getUserIdOrThrow(), username);
    return Response.ok().build();
  }
}
