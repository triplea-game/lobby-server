package org.triplea.web.socket;

import jakarta.websocket.CloseReason;
import java.net.InetAddress;

public interface WebSocketSession {
  boolean isOpen();

  InetAddress getRemoteAddress();

  default void close() {
    close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Session closed by server"));
  }

  void close(CloseReason closeReason);

  void sendText(String text);

  String getId();
}
