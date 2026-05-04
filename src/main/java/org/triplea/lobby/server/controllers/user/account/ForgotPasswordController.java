package org.triplea.lobby.server.controllers.user.account;

import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.util.function.BiFunction;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jdbi.v3.core.Jdbi;
import org.triplea.http.client.forgot.password.ForgotPasswordClient;
import org.triplea.http.client.forgot.password.ForgotPasswordRequest;
import org.triplea.http.client.forgot.password.ForgotPasswordResponse;
import org.triplea.lobby.server.HttpController;
import org.triplea.lobby.server.IpAddressExtractor;
import org.triplea.modules.forgot.password.ForgotPasswordModule;

/** Http controller that binds the error upload endpoint with the error report upload handler. */
@ApplicationScoped
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ForgotPasswordController extends HttpController {

  @ConfigProperty(name = "app.send-emails-enabled", defaultValue = "false")
  boolean sendEmailsEnabled;

  @ConfigProperty(name = "app.smtp-host", defaultValue = "172.17.0.1")
  String smtpHost;

  @ConfigProperty(name = "app.smtp-port", defaultValue = "25")
  int smtpPort;

  @Inject Jdbi jdbi;

  private BiFunction<String, ForgotPasswordRequest, String> forgotPasswordModule;

  @PostConstruct
  void init() {
    forgotPasswordModule = ForgotPasswordModule.build(sendEmailsEnabled, jdbi, smtpHost, smtpPort);
  }

  @POST
  @Path(ForgotPasswordClient.FORGOT_PASSWORD_PATH)
  public ForgotPasswordResponse requestTempPassword(
      @Context final RoutingContext routingContext,
      final ForgotPasswordRequest forgotPasswordRequest) {

    if (forgotPasswordRequest.getUsername() == null || forgotPasswordRequest.getEmail() == null) {
      throw new jakarta.ws.rs.BadRequestException("Missing username or email in request");
    }
    return ForgotPasswordResponse.builder()
        .responseMessage(
            forgotPasswordModule.apply(
                IpAddressExtractor.extractIpAddress(routingContext), forgotPasswordRequest))
        .build();
  }
}
