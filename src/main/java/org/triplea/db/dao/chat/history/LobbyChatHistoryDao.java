package org.triplea.db.dao.chat.history;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.LoggerFactory;
import org.triplea.http.client.lobby.web.socket.messages.envelopes.chat.ChatReceivedMessage;

/** Lobby chat history records lobby chat messages. */
@ApplicationScoped
@RequiredArgsConstructor
public class LobbyChatHistoryDao {
  static final int MESSAGE_COLUMN_LENGTH = 240;

  private final Jdbi jdbi;

  public void recordMessage(ChatReceivedMessage chatReceivedMessage, int apiKeyId) {
    final String truncated =
        chatReceivedMessage
            .getMessage()
            .substring(
                0, Math.min(chatReceivedMessage.getMessage().length(), MESSAGE_COLUMN_LENGTH));
    CompletableFuture.runAsync(
            () -> insertMessage(chatReceivedMessage.getSender(), apiKeyId, truncated))
        .exceptionally(
            e -> {
              LoggerFactory.getLogger("triplea.dao")
                  .error("Error recording chat message in database history table", e);
              return null;
            });
  }

  /**
   * Stores a chat message record to database.
   *
   * @param username The name of the user that sent the chat message.
   * @param apiKeyId The ID of the API key the user used to sign in to the lobby. This is useful for
   *     cross-referencing to know the users IP and system-id.
   * @param message The chat message contents.
   */
  void insertMessage(String username, int apiKeyId, String message) {
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    """
                    insert into lobby_chat_history (username, lobby_api_key_id, message)
                    values(:username, :apiKeyId, :message)
                    """)
                .bind("username", username)
                .bind("apiKeyId", apiKeyId)
                .bind("message", message)
                .execute());
  }
}
