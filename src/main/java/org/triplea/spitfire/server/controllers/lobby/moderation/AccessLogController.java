package org.triplea.spitfire.server.controllers.lobby.moderation;

import com.google.common.base.Preconditions;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import javax.annotation.Nonnull;
import lombok.Builder;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.http.client.lobby.moderator.toolbox.log.AccessLogRequest;
import org.triplea.http.client.lobby.moderator.toolbox.log.ToolboxAccessLogClient;
import org.triplea.modules.moderation.access.log.AccessLogService;
import org.triplea.spitfire.server.HttpController;

/** Controller to query the access log table, for us by moderators. */
@Builder
@RolesAllowed(UserRole.MODERATOR)
public class AccessLogController extends HttpController {
  @Nonnull private final AccessLogService accessLogService;

  public static AccessLogController build(final Jdbi jdbi) {
    return AccessLogController.builder() //
        .accessLogService(AccessLogService.build(jdbi))
        .build();
  }

  @POST
  @Path(ToolboxAccessLogClient.FETCH_ACCESS_LOG_PATH)
  public Response fetchAccessLog(final AccessLogRequest accessLogRequest) {
    Preconditions.checkNotNull(accessLogRequest);
    Preconditions.checkNotNull(accessLogRequest.getAccessLogSearchRequest());
    Preconditions.checkNotNull(accessLogRequest.getPagingParams());
    return Response.ok().entity(accessLogService.fetchAccessLog(accessLogRequest)).build();
  }
}
