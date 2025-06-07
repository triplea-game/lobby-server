package org.triplea.db.dao.chat.history;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.slf4j.LoggerFactory;
import org.triplea.http.client.web.socket.messages.envelopes.chat.ChatReceivedMessage;
import org.triplea.java.StringUtils;
import org.triplea.java.concurrency.AsyncRunner;

/** Lobby chat history records lobby chat messages. */
public interface LobbyChatHistoryDao {
  int MESSAGE_COLUMN_LENGTH = 240;

  default void recordMessage(ChatReceivedMessage chatReceivedMessage, int apiKeyId) {
    AsyncRunner.runAsync(
            () ->
                insertMessage(
                    chatReceivedMessage.getSender().getValue(),
                    apiKeyId,
                    StringUtils.truncate(chatReceivedMessage.getMessage(), MESSAGE_COLUMN_LENGTH)))
        .exceptionally(
            e ->
                LoggerFactory.getLogger("triplea.dao")
                    .error("Error recording chat message in database history table", e));
  }

  /**
   * Stores a chat message record to database.
   *
   * @param username The name of the user that sent the chat message.
   * @param apiKeyId The ID of the API key the user used to sign in to the lobby. This is useful for
   *     cross-referencing to know the users IP and system-id.
   * @param message The chat message contents.
   */
  @SqlUpdate(
      "insert into lobby_chat_history (username, lobby_api_key_id, message) "
          + "values(:username, :apiKeyId, :message)")
  void insertMessage(
      @Bind("username") String username,
      @Bind("apiKeyId") int apiKeyId,
      @Bind("message") String message);
}
