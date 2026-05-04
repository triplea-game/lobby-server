package org.triplea.lobby.server.access.authorization;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.time.Clock;
import java.time.Duration;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.ban.BanLookupRecord;
import org.triplea.db.dao.user.ban.UserBanDao;
import org.triplea.http.client.LobbyHttpClientConfig;
import org.triplea.http.client.lobby.moderator.BanDurationFormatter;
import org.triplea.lobby.server.IpAddressExtractor;

@Provider
@PreMatching
@ApplicationScoped
public class BannedPlayerFilter implements ContainerRequestFilter {

  @Inject Jdbi jdbi;

  @Context private RoutingContext routingContext;

  private UserBanDao userBanDao;
  private Clock clock;

  /** CDI no-arg constructor. */
  public BannedPlayerFilter() {
    this.clock = Clock.systemUTC();
  }

  /** Test constructor — bypasses CDI; supplies all dependencies directly. */
  @VisibleForTesting
  BannedPlayerFilter(
      final UserBanDao userBanDao, final Clock clock, final RoutingContext routingContext) {
    this.userBanDao = userBanDao;
    this.clock = clock;
    this.routingContext = routingContext;
  }

  @PostConstruct
  void init() {
    userBanDao = new UserBanDao(jdbi);
  }

  @Override
  public void filter(final ContainerRequestContext requestContext) {
    final String systemId =
        Strings.emptyToNull(
            routingContext.request().getHeader(LobbyHttpClientConfig.SYSTEM_ID_HEADER));
    if (systemId == null) {
      requestContext.abortWith(
          Response.status(Response.Status.UNAUTHORIZED).entity("Invalid request").build());
    } else {
      userBanDao
          .lookupBan(IpAddressExtractor.extractIpAddress(routingContext), systemId)
          .map(this::formatBanMessage)
          .ifPresent(
              banMessage ->
                  requestContext.abortWith(
                      Response.status(Response.Status.UNAUTHORIZED).entity(banMessage).build()));
    }
  }

  private String formatBanMessage(final BanLookupRecord banLookupRecord) {
    final long banMinutes =
        Duration.between(clock.instant(), banLookupRecord.getBanExpiry()).toMinutes();
    final String banDuration = BanDurationFormatter.formatBanMinutes(banMinutes);
    return String.format("Banned %s,  Ban-ID: %s", banDuration, banLookupRecord.getPublicBanId());
  }
}
