package org.triplea.modules.moderation.ban.user;

import static org.mockito.Mockito.verify;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.triplea.web.socket.SessionSet;

@ExtendWith(MockitoExtension.class)
class BannedPlayerEventHandlerTest {

  private static final InetAddress IP = InetAddresses.forString("99.88.77.66");
  @Mock private SessionSet sessionSet;

  @Test
  void fireBannedEvent() {
    final BannedPlayerEventHandler bannedPlayerEventHandler =
        BannedPlayerEventHandler.builder().sessionSets(Set.of(sessionSet)).build();

    bannedPlayerEventHandler.fireBannedEvent(IP);

    verify(sessionSet).closeSessionsByIp(IP);
  }
}
