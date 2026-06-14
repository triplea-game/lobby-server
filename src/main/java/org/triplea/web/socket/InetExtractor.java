package org.triplea.web.socket;

import com.google.common.annotations.VisibleForTesting;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InetExtractor {

  @VisibleForTesting
  public static final String IP_ADDRESS_KEY = "jakarta.websocket.endpoint.remoteAddress";

  /**
   * Returns the IP address of the session from the provided 'userSession' map. It is expected that
   * for the websocket library that we use that we will always find an IP address.
   */
  public static InetAddress extract(final Map<String, Object> userSession) {
    final String ip = String.valueOf(userSession.get(IP_ADDRESS_KEY));
    try {
      return InetAddress.getByName(ip);
    } catch (final UnknownHostException e) {
      throw new AssertionError("Unexpected bad hostname: " + ip, e);
    }
  }
}
