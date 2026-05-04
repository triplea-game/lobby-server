package org.triplea.web.socket;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import lombok.experimental.UtilityClass;
import org.slf4j.LoggerFactory;

/**
 * Converts 'session' objects that we receive as parameters to websocket servers to implementations
 * of the unified interface 'WebSocketSession'.
 */
@UtilityClass
public class WebSocketSessionAdapter {
  static WebSocketSession fromSession(final Session session) {
    return new WebSocketSession() {
      @Override
      public boolean isOpen() {
        return session.isOpen();
      }

      @Override
      public InetAddress getRemoteAddress() {
        return InetExtractor.extract(session.getUserProperties());
      }

      @Override
      public void close(final CloseReason closeReason) {
        try {
          session.close(closeReason);
        } catch (final IOException e) {
          LoggerFactory.getLogger(WebSocketSessionAdapter.class)
              .warn("Error closing websocket session", e);
        }
      }

      @Override
      public void sendText(final String text) {
        try {
          session.getAsyncRemote().sendText(text).get();
        } catch (final InterruptedException | ExecutionException e) {
          LoggerFactory.getLogger(WebSocketSessionAdapter.class)
              .error("Error sending websocket message", e);
        }
      }

      @Override
      public String getId() {
        return session.getId();
      }
    };
  }
}
