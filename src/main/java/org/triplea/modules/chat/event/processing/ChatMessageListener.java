package org.triplea.modules.chat.event.processing;

import java.net.InetAddress;
import java.time.Instant;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.triplea.db.dao.chat.history.LobbyChatHistoryDao;
import org.triplea.http.client.web.socket.messages.envelopes.chat.ChatEventReceivedMessage;
import org.triplea.http.client.web.socket.messages.envelopes.chat.ChatReceivedMessage;
import org.triplea.http.client.web.socket.messages.envelopes.chat.ChatSentMessage;
import org.triplea.modules.chat.ChatterSession;
import org.triplea.modules.chat.Chatters;
import org.triplea.web.socket.WebSocketMessageContext;

@Builder
@Slf4j
public class ChatMessageListener implements Consumer<WebSocketMessageContext<ChatSentMessage>> {

  @Nonnull private final Chatters chatters;
  @Nonnull private final LobbyChatHistoryDao lobbyChatHistoryDao;

  public static ChatMessageListener build(final Chatters chatters, final Jdbi jdbi) {
    return ChatMessageListener.builder()
        .chatters(chatters)
        .lobbyChatHistoryDao(jdbi.onDemand(LobbyChatHistoryDao.class))
        .build();
  }

  @Override
  public void accept(final WebSocketMessageContext<ChatSentMessage> messageContext) {
    final InetAddress chatterIp = messageContext.getSenderSession().getRemoteAddress();

    chatters
        .lookupPlayerBySession(messageContext.getSenderSession())
        .ifPresent(
            session ->
                chatters
                    .getPlayerMuteExpiration(chatterIp)
                    .ifPresentOrElse(
                        expiry -> sendResponseToMutedPlayer(expiry, messageContext),
                        () -> recordAndBroadcastMessageToAllPlayers(session, messageContext)));
  }

  private void sendResponseToMutedPlayer(
      final Instant muteExpiry, final WebSocketMessageContext<ChatSentMessage> messageContext) {
    messageContext.sendResponse(
        new ChatEventReceivedMessage(PlayerIsMutedMessage.build(muteExpiry)));
  }

  private void recordAndBroadcastMessageToAllPlayers(
      final ChatterSession session, final WebSocketMessageContext<ChatSentMessage> messageContext) {
    final var chatReceivedMessage =
        new ChatReceivedMessage(
            session.getChatParticipant().getUserName(),
            messageContext.getMessage().getChatMessage());

    lobbyChatHistoryDao.recordMessage(chatReceivedMessage, session.getApiKeyId());
    messageContext.broadcastMessage(chatReceivedMessage);
  }
}
