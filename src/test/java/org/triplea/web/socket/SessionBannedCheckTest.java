package org.triplea.web.socket;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import com.google.common.net.InetAddresses;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.triplea.db.dao.user.ban.UserBanDao;

@ExtendWith(MockitoExtension.class)
class SessionBannedCheckTest {

  @Mock private UserBanDao userBanDao;

  @InjectMocks private SessionBannedCheck sessionBannedCheck;

  @Test
  void notBanned() {
    givenSessionIsBanned(false, "1.1.1.1");

    assertThat(sessionBannedCheck.test(InetAddresses.forString("1.1.1.1")), is(false));
  }

  @Test
  void banned() {
    givenSessionIsBanned(true, "1.1.1.1");

    assertThat(sessionBannedCheck.test(InetAddresses.forString("1.1.1.1")), is(true));
  }

  private void givenSessionIsBanned(final boolean isBanned, final String ipAddress) {
    when(userBanDao.isBannedByIp(ipAddress)).thenReturn(isBanned);
  }
}
