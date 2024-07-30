package org.triplea.spitfire.server.controllers.lobby.moderation;

import com.google.common.base.Preconditions;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Builder;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.role.UserRole;
import org.triplea.http.client.lobby.moderator.toolbox.PagingParams;
import org.triplea.http.client.lobby.moderator.toolbox.log.ModeratorEvent;
import org.triplea.http.client.lobby.moderator.toolbox.log.ToolboxEventLogClient;
import org.triplea.modules.moderation.audit.history.ModeratorAuditHistoryService;
import org.triplea.spitfire.server.HttpController;

/** Http server endpoints for accessing and returning moderator audit history rows. */
@Builder
@RolesAllowed(UserRole.MODERATOR)
public class ModeratorAuditHistoryController extends HttpController {
  @Nonnull private final ModeratorAuditHistoryService moderatorAuditHistoryService;

  public static ModeratorAuditHistoryController build(final Jdbi jdbi) {
    return ModeratorAuditHistoryController.builder()
        .moderatorAuditHistoryService(ModeratorAuditHistoryService.build(jdbi))
        .build();
  }

  /**
   * Use this method to retrieve moderator audit history rows. Presents a paged interface.
   *
   * @param pagingParams Parameter JSON object for page number and page size.
   */
  @POST
  @Path(ToolboxEventLogClient.AUDIT_HISTORY_PATH)
  public Response lookupAuditHistory(final PagingParams pagingParams) {
    Preconditions.checkArgument(pagingParams != null);
    Preconditions.checkArgument(pagingParams.getRowNumber() >= 0);
    Preconditions.checkArgument(pagingParams.getPageSize() > 0);

    final List<ModeratorEvent> moderatorEvents =
        moderatorAuditHistoryService.lookupHistory(
            pagingParams.getRowNumber(), pagingParams.getPageSize());

    return Response.status(200).entity(moderatorEvents).build();
  }
}
