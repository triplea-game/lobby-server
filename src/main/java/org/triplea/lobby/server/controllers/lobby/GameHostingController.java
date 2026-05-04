package org.triplea.lobby.server.controllers.lobby;

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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Function;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.api.key.GameHostingApiKeyDaoWrapper;
import org.triplea.domain.data.ApiKey;
import org.triplea.http.client.lobby.game.hosting.request.GameHostingClient;
import org.triplea.http.client.lobby.game.hosting.request.GameHostingResponse;
import org.triplea.lobby.server.HttpController;
import org.triplea.lobby.server.IpAddressExtractor;

/**
 * Provides an endpoint where an independent connection can be established, provides an API key to
 * unauthenticated users that can then be used to post a game. Banning rules are verified to ensure
 * banned users cannot post games.
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("RestResourceMethodInspection")
@ApplicationScoped
public class GameHostingController extends HttpController {

  @Inject Jdbi jdbi;

  private Function<InetAddress, ApiKey> apiKeySupplier;

  @PostConstruct
  void init() {
    apiKeySupplier = GameHostingApiKeyDaoWrapper.build(jdbi)::newGameHostKey;
  }

  @POST
  @Path(GameHostingClient.GAME_HOSTING_REQUEST_PATH)
  public GameHostingResponse hostingRequest(@Context final RoutingContext routingContext) {
    String remoteIp = IpAddressExtractor.extractIpAddress(routingContext);
    try {
      return GameHostingResponse.builder()
          .apiKey(apiKeySupplier.apply(InetAddress.getByName(remoteIp)).getValue())
          .publicVisibleIp(remoteIp)
          .build();
    } catch (final UnknownHostException e) {
      throw new IllegalArgumentException("Invalid IP address in request: " + remoteIp, e);
    }
  }
}
