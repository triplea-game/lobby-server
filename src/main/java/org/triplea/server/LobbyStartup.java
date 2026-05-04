package org.triplea.server;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.triplea.modules.chat.ChatMessagingService;
import org.triplea.modules.chat.Chatters;
import org.triplea.server.qualifier.PlayerConnections;
import org.triplea.web.socket.WebSocketMessagingBus;

/**
 * Performs one-time wiring at application startup: attaches chat message listeners to the
 * player-connections messaging bus.
 *
 * <p>WebSocket endpoints ({@link org.triplea.web.socket.GameConnectionWebSocket} and {@link
 * org.triplea.web.socket.PlayerConnectionWebSocket}) receive their {@link
 * org.triplea.web.socket.GenericWebSocket} instances via CDI injection rather than the static
 * registry that was used in the DropWizard era.
 */
@ApplicationScoped
@Slf4j
public class LobbyStartup {

  @Inject Jdbi jdbi;

  @Inject @PlayerConnections WebSocketMessagingBus playerConnectionMessagingBus;

  @Inject Chatters chatters;

  void onStart(@Observes final StartupEvent event) {
    ChatMessagingService.build(chatters, jdbi).configure(playerConnectionMessagingBus);
    log.info("Lobby server CDI wiring complete.");
  }
}
