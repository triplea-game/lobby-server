package org.triplea.server;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.triplea.modules.chat.Chatters;
import org.triplea.modules.game.listing.GameListing;
import org.triplea.server.qualifier.GameConnections;
import org.triplea.server.qualifier.PlayerConnections;
import org.triplea.web.socket.GenericWebSocket;
import org.triplea.web.socket.SessionBannedCheck;
import org.triplea.web.socket.WebSocketMessagingBus;

/**
 * CDI producer class for the singleton beans shared across multiple controllers and websocket
 * endpoints. Each method annotated with {@code @Produces @ApplicationScoped} registers one bean
 * available for injection anywhere in the application.
 */
@ApplicationScoped
public class LobbyBeans {

  @Inject Jdbi jdbi;

  @Produces
  @ApplicationScoped
  @GameConnections
  public WebSocketMessagingBus gameConnectionMessagingBus() {
    return new WebSocketMessagingBus();
  }

  @Produces
  @ApplicationScoped
  @PlayerConnections
  public WebSocketMessagingBus playerConnectionMessagingBus() {
    return new WebSocketMessagingBus();
  }

  @Produces
  @ApplicationScoped
  public Chatters chatters() {
    return Chatters.build();
  }

  @Produces
  @ApplicationScoped
  public SessionBannedCheck sessionBannedCheck() {
    return SessionBannedCheck.build(jdbi);
  }

  @Produces
  @ApplicationScoped
  @GameConnections
  public GenericWebSocket gameConnectionWebSocket(
      @GameConnections final WebSocketMessagingBus messagingBus,
      final SessionBannedCheck sessionBannedCheck) {
    return new GenericWebSocket(messagingBus, sessionBannedCheck);
  }

  @Produces
  @ApplicationScoped
  @PlayerConnections
  public GenericWebSocket playerConnectionWebSocket(
      @PlayerConnections final WebSocketMessagingBus messagingBus,
      final SessionBannedCheck sessionBannedCheck) {
    return new GenericWebSocket(messagingBus, sessionBannedCheck);
  }

  @Produces
  @ApplicationScoped
  public GameListing gameListing(@PlayerConnections final WebSocketMessagingBus playerBus) {
    return GameListing.build(jdbi, playerBus);
  }
}
