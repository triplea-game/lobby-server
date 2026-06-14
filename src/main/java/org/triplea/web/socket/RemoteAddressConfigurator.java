package org.triplea.web.socket;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Captures the client IP from the WebSocket handshake and stores it in user properties.
 *
 * <p>The Quarkus Vert.x WebSocket implementation does not expose the TCP remote address via
 * HttpServletRequest. Instead, we read the {@code X-Forwarded-For} header set by the reverse proxy
 * (requires {@code quarkus.http.proxy.proxy-address-forwarding=true} in application.properties).
 *
 * <p>If {@code X-Forwarded-For} is absent and {@code app.websocket.ip-enforcing} is {@code true}
 * (the default), the IP key is left unset and the connection will be rejected in {@link
 * GenericWebSocket#onOpen}. Set {@code app.websocket.ip-enforcing=false} (e.g., in the test
 * profile) to fall back to the loopback address instead, which allows direct connections that
 * bypass the reverse proxy.
 */
@Slf4j
public class RemoteAddressConfigurator extends ServerEndpointConfig.Configurator {

  @Override
  public void modifyHandshake(
      final ServerEndpointConfig sec,
      final HandshakeRequest request,
      final HandshakeResponse response) {
    final List<String> forwardedFor = request.getHeaders().get("X-Forwarded-For");
    log.info("IP address forwarded for: {}", forwardedFor);
    if (forwardedFor != null && !forwardedFor.isEmpty()) {
      // X-Forwarded-For may be "client, proxy1, proxy2" — take the leftmost (original client)
      final String ip = forwardedFor.get(0).split(",")[0].trim();
      sec.getUserProperties().put(InetExtractor.IP_ADDRESS_KEY, ip);
    }
  }
}
