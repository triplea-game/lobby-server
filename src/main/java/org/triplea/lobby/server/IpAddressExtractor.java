package org.triplea.lobby.server;

import io.vertx.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IpAddressExtractor {

  /**
   * Extracts the IP address from a Vert.x {@link RoutingContext}.
   *
   * <p>The remote address can be surrounded by square brackets (IPv6); this method strips them.
   *
   * @return valid IP address of the remote machine making the request
   */
  public String extractIpAddress(RoutingContext routingContext) {
    return stripBrackets(routingContext.request().remoteAddress().host());
  }

  private String stripBrackets(String address) {
    return address.replaceAll("\\[", "").replaceAll("\\]", "");
  }
}
