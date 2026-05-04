package org.triplea.lobby.server.controllers.lobby.moderation;

import com.google.common.base.Preconditions;
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
import org.triplea.http.client.lobby.moderator.toolbox.banned.name.ToolboxUsernameBanClient;
import org.triplea.lobby.server.HttpController;
import org.triplea.modules.moderation.ban.name.UsernameBanService;

/** Endpoint for use by moderators to view, add and remove player username bans. */
@ApplicationScoped
@RolesAllowed(UserRole.MODERATOR)
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UsernameBanController extends HttpController {

  @Inject Jdbi jdbi;

  private UsernameBanService bannedNamesService;

  @PostConstruct
  void init() {
    bannedNamesService = UsernameBanService.build(jdbi);
  }

  @POST
  @Path(ToolboxUsernameBanClient.REMOVE_BANNED_USER_NAME_PATH)
  public Response removeBannedUsername(@Context final SecurityContext sc, final String username) {
    Preconditions.checkArgument(username != null && !username.isEmpty());
    return Response.status(
            bannedNamesService.removeUsernameBan(user(sc).getUserIdOrThrow(), username) ? 200 : 400)
        .build();
  }

  @POST
  @Path(ToolboxUsernameBanClient.ADD_BANNED_USER_NAME_PATH)
  public Response addBannedUsername(@Context final SecurityContext sc, final String username) {
    Preconditions.checkArgument(username != null && !username.isEmpty());
    return Response.status(
            bannedNamesService.addBannedUserName(
                    user(sc).getUserIdOrThrow(), username.toUpperCase())
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
