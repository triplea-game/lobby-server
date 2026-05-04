package org.triplea.lobby.server.controllers.user.account;

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
import org.triplea.http.client.lobby.user.account.FetchEmailResponse;
import org.triplea.http.client.lobby.user.account.UserAccountClient;
import org.triplea.java.ArgChecker;
import org.triplea.lobby.server.HttpController;
import org.triplea.modules.user.account.update.UpdateAccountService;

/** Controller providing endpoints for user account management. */
@ApplicationScoped
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UpdateAccountController extends HttpController {

  @Inject Jdbi jdbi;

  private UpdateAccountService userAccountService;

  @PostConstruct
  void init() {
    userAccountService = UpdateAccountService.build(jdbi);
  }

  @POST
  @Path(UserAccountClient.CHANGE_PASSWORD_PATH)
  @RolesAllowed(UserRole.PLAYER)
  public Response changePassword(@Context final SecurityContext sc, final String newPassword) {
    ArgChecker.checkNotEmpty(newPassword);
    Preconditions.checkArgument(user(sc).getUserIdOrThrow() > 0);
    userAccountService.changePassword(user(sc).getUserIdOrThrow(), newPassword);
    return Response.ok().build();
  }

  @GET
  @Path(UserAccountClient.FETCH_EMAIL_PATH)
  @RolesAllowed(UserRole.PLAYER)
  public FetchEmailResponse fetchEmail(@Context final SecurityContext sc) {
    Preconditions.checkArgument(user(sc).getUserIdOrThrow() > 0);
    return new FetchEmailResponse(userAccountService.fetchEmail(user(sc).getUserIdOrThrow()));
  }

  @POST
  @Path(UserAccountClient.CHANGE_EMAIL_PATH)
  @RolesAllowed(UserRole.PLAYER)
  public Response changeEmail(@Context final SecurityContext sc, final String newEmail) {
    ArgChecker.checkNotEmpty(newEmail);
    Preconditions.checkArgument(user(sc).getUserIdOrThrow() > 0);
    userAccountService.changeEmail(user(sc).getUserIdOrThrow(), newEmail);
    return Response.ok().build();
  }
}
