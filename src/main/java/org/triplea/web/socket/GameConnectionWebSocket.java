package org.triplea.web.socket;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.triplea.http.client.web.socket.WebsocketPaths;

@ServerEndpoint(WebsocketPaths.GAME_CONNECTIONS)
public class GameConnectionWebSocket {
  @OnOpen
  public void onOpen(final Session session) {
    GenericWebSocket.getInstance(this.getClass()).onOpen(session);
  }

  @OnMessage
  public void onMessage(final Session session, final String message) {
    GenericWebSocket.getInstance(this.getClass()).onMessage(session, message);
  }

  @OnClose
  public void onClose(final Session session, final CloseReason closeReason) {
    GenericWebSocket.getInstance(this.getClass()).onClose(session, closeReason);
  }

  @OnError
  public void onError(final Session session, final Throwable throwable) {
    GenericWebSocket.getInstance(this.getClass()).onError(session, throwable);
  }
}
