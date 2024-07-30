package org.triplea.spitfire.server.controllers.lobby.moderation;

import com.google.common.base.Preconditions;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.domain.data.PlayerChatId;
import org.triplea.http.client.lobby.moderator.ModeratorLobbyClient;
import org.triplea.http.client.lobby.moderator.MuteUserRequest;
import org.triplea.modules.chat.Chatters;
import org.triplea.spitfire.server.HttpController;

@AllArgsConstructor
@Builder
@RolesAllowed(UserRole.MODERATOR)
public class MuteUserController extends HttpController {

  private final Chatters chatters;

  public static MuteUserController build(final Chatters chatters) {
    return new MuteUserController(chatters);
  }

  @POST
  @Path(ModeratorLobbyClient.MUTE_USER)
  public Response muteUser(final MuteUserRequest muteUserRequest) {
    Preconditions.checkArgument(muteUserRequest != null);
    Preconditions.checkArgument(muteUserRequest.getPlayerChatId() != null);
    Preconditions.checkArgument(muteUserRequest.getMinutes() > 0);

    chatters.mutePlayer(
        PlayerChatId.of(muteUserRequest.getPlayerChatId()), muteUserRequest.getMinutes());
    return Response.ok().build();
  }
}
