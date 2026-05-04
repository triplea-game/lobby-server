package org.triplea.lobby.server.controllers.user.account;

import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.jdbi.v3.core.Jdbi;
import org.triplea.domain.data.LobbyConstants;
import org.triplea.http.client.LobbyHttpClientConfig;
import org.triplea.http.client.lobby.login.LobbyLoginClient;
import org.triplea.http.client.lobby.login.LobbyLoginResponse;
import org.triplea.http.client.lobby.login.LoginRequest;
import org.triplea.lobby.server.HttpController;
import org.triplea.lobby.server.IpAddressExtractor;
import org.triplea.modules.chat.Chatters;
import org.triplea.modules.user.account.login.LoginModule;

@ApplicationScoped
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginController extends HttpController {

  @Inject Jdbi jdbi;
  @Inject Chatters chatters;

  private LoginModule loginModule;

  @PostConstruct
  void init() {
    loginModule = LoginModule.build(jdbi, chatters);
  }

  @POST
  @Path(LobbyLoginClient.LOGIN_PATH)
  public LobbyLoginResponse login(
      @Context final RoutingContext routingContext, final LoginRequest loginRequest) {
    if (loginRequest == null) {
      throw new BadRequestException("Login request cannot be null");
    }
    if (loginRequest.getName() == null) {
      throw new BadRequestException("Username cannot be null");
    }
    if (loginRequest.getName().length() > LobbyConstants.USERNAME_MAX_LENGTH) {
      throw new BadRequestException("Username exceeds maximum length");
    }
    if (loginRequest.getName().length() < LobbyConstants.USERNAME_MIN_LENGTH) {
      throw new BadRequestException("Username is below minimum length");
    }

    return loginModule.doLogin(
        loginRequest,
        routingContext.request().getHeader(LobbyHttpClientConfig.SYSTEM_ID_HEADER),
        IpAddressExtractor.extractIpAddress(routingContext));
  }
}
