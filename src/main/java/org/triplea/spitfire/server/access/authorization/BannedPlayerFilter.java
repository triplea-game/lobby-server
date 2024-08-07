package org.triplea.spitfire.server.access.authorization;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.time.Clock;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.user.ban.BanLookupRecord;
import org.triplea.db.dao.user.ban.UserBanDao;
import org.triplea.dropwizard.common.IpAddressExtractor;
import org.triplea.http.client.LobbyHttpClientConfig;
import org.triplea.http.client.lobby.moderator.BanDurationFormatter;

@Provider
@PreMatching
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = @VisibleForTesting)
public class BannedPlayerFilter implements ContainerRequestFilter {

  private final UserBanDao userBanDao;
  private final Clock clock;

  @Context private HttpServletRequest request;

  public static BannedPlayerFilter newBannedPlayerFilter(final Jdbi jdbi) {
    return new BannedPlayerFilter(jdbi.onDemand(UserBanDao.class), Clock.systemUTC());
  }

  @Override
  public void filter(final ContainerRequestContext requestContext) {
    if (Strings.emptyToNull(request.getHeader(LobbyHttpClientConfig.SYSTEM_ID_HEADER)) == null) {
      // missing system id header, abort the request
      requestContext.abortWith(
          Response.status(Response.Status.UNAUTHORIZED).entity("Invalid request").build());

    } else {
      // check if user is banned, if so abort the request
      userBanDao
          .lookupBan(
              IpAddressExtractor.extractIpAddress(request),
              request.getHeader(LobbyHttpClientConfig.SYSTEM_ID_HEADER))
          .map(this::formatBanMessage)
          .ifPresent(
              banMessage ->
                  requestContext.abortWith(
                      Response.status(Response.Status.UNAUTHORIZED.getStatusCode(), banMessage)
                          .build()));
    }
  }

  private String formatBanMessage(final BanLookupRecord banLookupRecord) {
    final long banMinutes =
        Duration.between(clock.instant(), banLookupRecord.getBanExpiry()).toMinutes();
    final String banDuration = BanDurationFormatter.formatBanMinutes(banMinutes);

    return String.format("Banned %s,  Ban-ID: %s", banDuration, banLookupRecord.getPublicBanId());
  }
}
