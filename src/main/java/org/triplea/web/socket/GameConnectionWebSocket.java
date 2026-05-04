package org.triplea.web.socket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.triplea.http.client.web.socket.WebsocketPaths;
import org.triplea.server.qualifier.GameConnections;

@ApplicationScoped
@ServerEndpoint(
    value = WebsocketPaths.GAME_CONNECTIONS,
    configurator = RemoteAddressConfigurator.class)
public class GameConnectionWebSocket {
  @Inject @GameConnections GenericWebSocket genericWebSocket;

  @OnOpen
  public void onOpen(final Session session) {
    genericWebSocket.onOpen(session);
  }

  @OnMessage
  public void onMessage(final Session session, final String message) {
    genericWebSocket.onMessage(session, message);
  }

  @OnClose
  public void onClose(final Session session, final CloseReason closeReason) {
    genericWebSocket.onClose(session, closeReason);
  }

  @OnError
  public void onError(final Session session, final Throwable throwable) {
    genericWebSocket.onError(session, throwable);
  }
}
