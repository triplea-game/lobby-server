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
import org.triplea.http.client.lobby.moderator.BanPlayerRequest;
import org.triplea.http.client.lobby.moderator.ModeratorLobbyClient;
import org.triplea.http.client.lobby.moderator.toolbox.banned.user.ToolboxUserBanClient;
import org.triplea.http.client.lobby.moderator.toolbox.banned.user.UserBanParams;
import org.triplea.modules.chat.Chatters;
import org.triplea.modules.moderation.ban.user.UserBanService;
import org.triplea.spitfire.server.HttpController;
import org.triplea.spitfire.server.access.authentication.AuthenticatedUser;
import org.triplea.web.socket.WebSocketMessagingBus;

/** Controller for endpoints to manage user bans, to be used by moderators. */
@Builder
@RolesAllowed(UserRole.MODERATOR)
public class UserBanController extends HttpController {
  @Nonnull private final UserBanService bannedUsersService;

  public static UserBanController build(
      final Jdbi jdbi,
      final Chatters chatters,
      final WebSocketMessagingBus chatMessagingBus,
      final WebSocketMessagingBus gameMessagingBus) {
    return UserBanController.builder()
        .bannedUsersService(
            UserBanService.builder()
                .jdbi(jdbi)
                .chatters(chatters)
                .chatMessagingBus(chatMessagingBus)
                .gameMessagingBus(gameMessagingBus)
                .build())
        .build();
  }

  @GET
  @Path(ToolboxUserBanClient.GET_USER_BANS_PATH)
  public Response getUserBans() {
    return Response.ok().entity(bannedUsersService.getBannedUsers()).build();
  }

  @POST
  @Path(ToolboxUserBanClient.REMOVE_USER_BAN_PATH)
  public Response removeUserBan(
      @Auth final AuthenticatedUser authenticatedUser, final String banId) {
    Preconditions.checkArgument(banId != null);

    final boolean removed =
        bannedUsersService.removeUserBan(authenticatedUser.getUserIdOrThrow(), banId);
    return Response.status(removed ? 200 : 400).build();
  }

  /** Endpoint to add a user ban. Returns 200 if the ban is added, 400 if not. */
  @POST
  @Path(ToolboxUserBanClient.BAN_USER_PATH)
  public Response banUser(
      @Auth final AuthenticatedUser authenticatedUser, final UserBanParams banUserParams) {
    Preconditions.checkArgument(banUserParams != null);
    Preconditions.checkArgument(banUserParams.getSystemId() != null);
    Preconditions.checkArgument(banUserParams.getIp() != null);
    Preconditions.checkArgument(banUserParams.getUsername() != null);
    Preconditions.checkArgument(banUserParams.getMinutesToBan() > 0);

    bannedUsersService.banUser(authenticatedUser.getUserIdOrThrow(), banUserParams);
    return Response.ok().build();
  }

  @POST
  @Path(ModeratorLobbyClient.BAN_PLAYER_PATH)
  public Response banPlayer(
      @Auth final AuthenticatedUser authenticatedUser, final BanPlayerRequest banPlayerRequest) {
    Preconditions.checkNotNull(banPlayerRequest);
    Preconditions.checkNotNull(banPlayerRequest.getPlayerChatId());
    Preconditions.checkArgument(banPlayerRequest.getBanMinutes() > 0);

    bannedUsersService.banUser(authenticatedUser.getUserIdOrThrow(), banPlayerRequest);
    return Response.ok().build();
  }
}
