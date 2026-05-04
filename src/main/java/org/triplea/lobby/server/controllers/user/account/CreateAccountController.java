package org.triplea.lobby.server.controllers.user.account;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.function.Function;
import org.jdbi.v3.core.Jdbi;
import org.triplea.domain.data.LobbyConstants;
import org.triplea.http.client.lobby.login.CreateAccountRequest;
import org.triplea.http.client.lobby.login.CreateAccountResponse;
import org.triplea.http.client.lobby.login.LobbyLoginClient;
import org.triplea.lobby.server.HttpController;
import org.triplea.lobby.server.controllers.ArgConditions;
import org.triplea.modules.user.account.create.CreateAccountModule;

@ApplicationScoped
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CreateAccountController extends HttpController {

  @Inject Jdbi jdbi;

  private Function<CreateAccountRequest, CreateAccountResponse> createAccountModule;

  @PostConstruct
  void init() {
    createAccountModule = CreateAccountModule.build(jdbi);
  }

  @POST
  @Path(LobbyLoginClient.CREATE_ACCOUNT)
  public CreateAccountResponse createAccount(final CreateAccountRequest createAccountRequest) {
    ArgConditions.assertNotNull(createAccountRequest, "Create account request cannot be null");
    ArgConditions.assertNotNull(createAccountRequest.getUsername(), "Username cannot be null");
    ArgConditions.assertTrue(
        createAccountRequest.getUsername().length() <= LobbyConstants.USERNAME_MAX_LENGTH,
        "Username exceeds maximum length");
    ArgConditions.assertTrue(
        createAccountRequest.getUsername().length() >= LobbyConstants.USERNAME_MIN_LENGTH,
        "Username is below minimum length");
    ArgConditions.assertNotNull(createAccountRequest.getEmail(), "Email cannot be null");
    ArgConditions.assertTrue(
        createAccountRequest.getEmail().length() <= LobbyConstants.EMAIL_MAX_LENGTH,
        "Email exceeds maximum length");
    ArgConditions.assertNotNull(createAccountRequest.getPassword(), "Password cannot be null");
    ArgConditions.assertTrue(
        createAccountRequest.getPassword().length() >= LobbyConstants.PASSWORD_MIN_LENGTH,
        "Password is below minimum length");

    return createAccountModule.apply(createAccountRequest);
  }
}
