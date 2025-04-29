package org.triplea.modules.user.account.login;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import com.github.database.rider.junit5.DBUnitExtension;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.triplea.IntegTestExtension;

@RequiredArgsConstructor
@ExtendWith(IntegTestExtension.class)
@ExtendWith(DBUnitExtension.class)
class LobbyLoginMessageDaoTest {

  private final LobbyLoginMessageDao lobbyLoginMessageDao;

  @Test
  void selectLobbyMessage() {
    assertThat(lobbyLoginMessageDao.get(), is(notNullValue()));
  }
}
