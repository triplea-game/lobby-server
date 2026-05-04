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
import org.triplea.http.client.lobby.moderator.BanPlayerRequest;
import org.triplea.http.client.lobby.moderator.ModeratorLobbyClient;
import org.triplea.http.client.lobby.moderator.toolbox.banned.user.ToolboxUserBanClient;
import org.triplea.http.client.lobby.moderator.toolbox.banned.user.UserBanParams;
import org.triplea.lobby.server.HttpController;
import org.triplea.modules.chat.Chatters;
import org.triplea.modules.moderation.ban.user.UserBanService;
import org.triplea.server.qualifier.GameConnections;
import org.triplea.server.qualifier.PlayerConnections;
import org.triplea.web.socket.WebSocketMessagingBus;

/** Controller for endpoints to manage user bans, to be used by moderators. */
@ApplicationScoped
@RolesAllowed(UserRole.MODERATOR)
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserBanController extends HttpController {

  @Inject Jdbi jdbi;
  @Inject Chatters chatters;

  @Inject @PlayerConnections WebSocketMessagingBus chatMessagingBus;

  @Inject @GameConnections WebSocketMessagingBus gameMessagingBus;

  private UserBanService bannedUsersService;

  @PostConstruct
  void init() {
    bannedUsersService =
        UserBanService.builder()
            .jdbi(jdbi)
            .chatters(chatters)
            .chatMessagingBus(chatMessagingBus)
            .gameMessagingBus(gameMessagingBus)
            .build();
  }

  @GET
  @Path(ToolboxUserBanClient.GET_USER_BANS_PATH)
  public Response getUserBans() {
    return Response.ok().entity(bannedUsersService.getBannedUsers()).build();
  }

  @POST
  @Path(ToolboxUserBanClient.REMOVE_USER_BAN_PATH)
  public Response removeUserBan(@Context final SecurityContext sc, final String banId) {
    Preconditions.checkArgument(banId != null);
    final boolean removed = bannedUsersService.removeUserBan(user(sc).getUserIdOrThrow(), banId);
    return Response.status(removed ? 200 : 400).build();
  }

  /** Endpoint to add a user ban. Returns 200 if the ban is added, 400 if not. */
  @POST
  @Path(ToolboxUserBanClient.BAN_USER_PATH)
  public Response banUser(@Context final SecurityContext sc, final UserBanParams banUserParams) {
    Preconditions.checkArgument(banUserParams != null);
    Preconditions.checkArgument(banUserParams.getSystemId() != null);
    Preconditions.checkArgument(banUserParams.getIp() != null);
    Preconditions.checkArgument(banUserParams.getUsername() != null);
    Preconditions.checkArgument(banUserParams.getMinutesToBan() > 0);
    bannedUsersService.banUser(user(sc).getUserIdOrThrow(), banUserParams);
    return Response.ok().build();
  }

  @POST
  @Path(ModeratorLobbyClient.BAN_PLAYER_PATH)
  public Response banPlayer(
      @Context final SecurityContext sc, final BanPlayerRequest banPlayerRequest) {
    Preconditions.checkNotNull(banPlayerRequest);
    Preconditions.checkNotNull(banPlayerRequest.getPlayerChatId());
    Preconditions.checkArgument(banPlayerRequest.getBanMinutes() > 0);
    bannedUsersService.banUser(user(sc).getUserIdOrThrow(), banPlayerRequest);
    return Response.ok().build();
  }
}
