package org.triplea.spitfire.server.access.authorization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.triplea.db.dao.user.ban.BanLookupRecord;
import org.triplea.db.dao.user.ban.UserBanDao;
import org.triplea.http.client.LobbyHttpClientConfig;

@SuppressWarnings("SameParameterValue")
@ExtendWith(MockitoExtension.class)
class BannedPlayerFilterTest {

  private static final Instant NOW_TIME = Instant.parse("2001-01-01T23:59:59.0Z");
  private static final String BAN_ID = "public-id";
  private static final String IP = "sample-ip";
  private static final String SYSTEM_ID = "system-id";

  @Mock private UserBanDao userBanDao;
  @Mock private Clock clock;
  @Mock private HttpServletRequest request;

  private BannedPlayerFilter bannedPlayerFilter;
  @Mock private ContainerRequestContext containerRequestContext;

  @BeforeEach
  void setup() {
    bannedPlayerFilter = new BannedPlayerFilter(userBanDao, clock, request);
  }

  void givenIpAndSystemId() {
    when(request.getRemoteAddr()).thenReturn(IP);
    when(request.getHeader(LobbyHttpClientConfig.SYSTEM_ID_HEADER)).thenReturn(SYSTEM_ID);
  }

  @Nested
  class NotBanned {
    @Test
    @DisplayName("IP and System ID are ok, verify request is *not* aborted")
    void notBanned() {
      givenIpAndSystemId();
      givenIpIsNotBanned();

      bannedPlayerFilter.filter(containerRequestContext);

      verifyRequestIsAllowed();
    }

    private void givenIpIsNotBanned() {
      when(userBanDao.lookupBan(IP, SYSTEM_ID)).thenReturn(Optional.empty());
    }

    private void verifyRequestIsAllowed() {
      verify(containerRequestContext, never()).abortWith(any());
    }
  }

  @Nested
  class Banned {
    @Test
    @DisplayName("IP or System ID are banned, verify request is aborted")
    void ipIsBanned() {
      givenIpAndSystemId();
      givenCurrentTimeIs(NOW_TIME);
      givenBannedWithExpiryAndBanIdentifier(NOW_TIME.plus(5, ChronoUnit.MINUTES), BAN_ID);

      bannedPlayerFilter.filter(containerRequestContext);

      verifyForbiddenRequest();
    }

    private void givenCurrentTimeIs(final Instant nowTime) {
      when(clock.instant()).thenReturn(nowTime);
    }

    private void givenBannedWithExpiryAndBanIdentifier(
        final Instant banExpiry, final String banId) {
      when(userBanDao.lookupBan(IP, SYSTEM_ID))
          .thenReturn(
              Optional.of(
                  BanLookupRecord.builder().banExpiry(banExpiry).publicBanId(banId).build()));
    }

    private void verifyForbiddenRequest() {
      final ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
      verify(containerRequestContext).abortWith(responseCaptor.capture());

      final Response response = responseCaptor.getValue();
      assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
      assertThat(response.getStatusInfo().getReasonPhrase(), containsString("5 minutes"));
      assertThat(response.getStatusInfo().getReasonPhrase(), containsString(BAN_ID));
    }
  }

  @Nested
  class MissingSystemId {
    @Test
    @DisplayName("Missing system ID is a bad request and should be rejected")
    void noSystemId() {
      when(request.getHeader(LobbyHttpClientConfig.SYSTEM_ID_HEADER)).thenReturn(null);

      bannedPlayerFilter.filter(containerRequestContext);

      verifyForbiddenRequest();
    }

    private void verifyForbiddenRequest() {
      final ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
      verify(containerRequestContext).abortWith(responseCaptor.capture());

      final Response response = responseCaptor.getValue();
      assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
    }
  }
}
