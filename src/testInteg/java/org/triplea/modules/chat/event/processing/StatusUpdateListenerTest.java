package org.triplea.modules.chat.event.processing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.triplea.http.client.lobby.web.socket.messages.envelopes.chat.ChatParticipant;
import org.triplea.http.client.lobby.web.socket.messages.envelopes.chat.PlayerStatusUpdateReceivedMessage;
import org.triplea.http.client.lobby.web.socket.messages.envelopes.chat.PlayerStatusUpdateSentMessage;
import org.triplea.modules.chat.ChatterSession;
import org.triplea.modules.chat.Chatters;
import org.triplea.modules.user.account.login.LoginModule;
import org.triplea.web.socket.WebSocketMessageContext;
import org.triplea.web.socket.WebSocketSession;

@ExtendWith(MockitoExtension.class)
class StatusUpdateListenerTest {
  @Mock private Chatters chatters;
  @InjectMocks private StatusUpdateListener statusUpdateListener;

  @Mock private WebSocketSession session;
  @Mock private WebSocketMessageContext<PlayerStatusUpdateSentMessage> messageContext;

  private final ArgumentCaptor<PlayerStatusUpdateReceivedMessage> messageCaptor =
      ArgumentCaptor.forClass(PlayerStatusUpdateReceivedMessage.class);

  @Test
  void noopIfChattersSessionDoesNotExist() {
    when(messageContext.getSenderSession()).thenReturn(session);
    when(chatters.lookupPlayerBySession(session)).thenReturn(Optional.empty());

    statusUpdateListener.accept(messageContext);

    verify(messageContext, never()).broadcastMessage(any());
  }

  @Test
  @DisplayName("If a player is in the chatter session, then we do relay their message")
  void ifPlayerSessionDoesExistThenRelayTheirMessage() {
    when(messageContext.getSenderSession()).thenReturn(session);
    when(messageContext.getMessage()).thenReturn(new PlayerStatusUpdateSentMessage("status"));
    givenChatterSession(
        session,
        ChatParticipant.builder().playerChatId(LoginModule.newId()).userName("user-name").build());

    statusUpdateListener.accept(messageContext);

    verify(messageContext).broadcastMessage(messageCaptor.capture());
    verifyMessageContents(messageCaptor.getValue());
  }

  private void givenChatterSession(
      final WebSocketSession session, final ChatParticipant chatParticipant) {
    when(chatters.lookupPlayerBySession(session))
        .thenReturn(
            Optional.of(
                ChatterSession.builder()
                    .session(session)
                    .chatParticipant(chatParticipant)
                    .apiKeyId(123)
                    .build()));
  }

  private static void verifyMessageContents(
      final PlayerStatusUpdateReceivedMessage chatReceivedMessage) {
    assertThat(chatReceivedMessage.getStatus()).isEqualTo("status");
    assertThat(chatReceivedMessage.getUserName()).isEqualTo("user-name");
  }
}
