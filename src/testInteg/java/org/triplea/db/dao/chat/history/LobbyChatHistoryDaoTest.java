package org.triplea.db.dao.chat.history;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.triplea.IntegTestExtension;

@RequiredArgsConstructor
@ExtendWith(IntegTestExtension.class)
@ExtendWith(DBUnitExtension.class)
class LobbyChatHistoryDaoTest {

  private final LobbyChatHistoryDao lobbyChatHistoryDao;

  @Test
  @DataSet(
      value =
          "lobby_chat_history/user_role.yml,"
              + "lobby_chat_history/lobby_user.yml,"
              + "lobby_chat_history/lobby_api_key.yml",
      useSequenceFiltering = false)
  @ExpectedDataSet("lobby_chat_history/lobby_chat_history_post_insert.yml")
  void insertChatMessage() {
    lobbyChatHistoryDao.insertMessage("username", 3000, "message");
  }
}
