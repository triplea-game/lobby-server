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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.http.client.lobby.moderator.toolbox.log.AccessLogRequest;
import org.triplea.http.client.lobby.moderator.toolbox.log.ToolboxAccessLogClient;
import org.triplea.lobby.server.HttpController;
import org.triplea.modules.moderation.access.log.AccessLogService;

/** Controller to query the access log table, for use by moderators. */
@ApplicationScoped
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(UserRole.MODERATOR)
public class AccessLogController extends HttpController {

  @Inject Jdbi jdbi;

  private AccessLogService accessLogService;

  @PostConstruct
  void init() {
    accessLogService = AccessLogService.build(jdbi);
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
