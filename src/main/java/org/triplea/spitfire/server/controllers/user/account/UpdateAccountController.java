package org.triplea.spitfire.server.controllers.user.account;

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
import org.triplea.http.client.lobby.user.account.FetchEmailResponse;
import org.triplea.http.client.lobby.user.account.UserAccountClient;
import org.triplea.java.ArgChecker;
import org.triplea.modules.user.account.update.UpdateAccountService;
import org.triplea.spitfire.server.HttpController;
import org.triplea.spitfire.server.access.authentication.AuthenticatedUser;

/** Controller providing endpoints for user account management. */
@Builder
public class UpdateAccountController extends HttpController {
  @Nonnull private final UpdateAccountService userAccountService;

  /** Instantiates controller with dependencies. */
  public static UpdateAccountController build(final Jdbi jdbi) {
    return UpdateAccountController.builder()
        .userAccountService(UpdateAccountService.build(jdbi))
        .build();
  }

  @POST
  @Path(UserAccountClient.CHANGE_PASSWORD_PATH)
  @RolesAllowed(UserRole.PLAYER)
  public Response changePassword(
      @Auth final AuthenticatedUser authenticatedUser, final String newPassword) {
    ArgChecker.checkNotEmpty(newPassword);
    Preconditions.checkArgument(authenticatedUser.getUserIdOrThrow() > 0);

    userAccountService.changePassword(authenticatedUser.getUserIdOrThrow(), newPassword);
    return Response.ok().build();
  }

  @GET
  @Path(UserAccountClient.FETCH_EMAIL_PATH)
  @RolesAllowed(UserRole.PLAYER)
  public FetchEmailResponse fetchEmail(@Auth final AuthenticatedUser authenticatedUser) {
    Preconditions.checkArgument(authenticatedUser.getUserIdOrThrow() > 0);

    return new FetchEmailResponse(
        userAccountService.fetchEmail(authenticatedUser.getUserIdOrThrow()));
  }

  @POST
  @Path(UserAccountClient.CHANGE_EMAIL_PATH)
  @RolesAllowed(UserRole.PLAYER)
  public Response changeEmail(
      @Auth final AuthenticatedUser authenticatedUser, final String newEmail) {
    ArgChecker.checkNotEmpty(newEmail);
    Preconditions.checkArgument(authenticatedUser.getUserIdOrThrow() > 0);

    userAccountService.changeEmail(authenticatedUser.getUserIdOrThrow(), newEmail);
    return Response.ok().build();
  }
}
