package org.triplea.modules.user.account.login;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import com.github.database.rider.junit5.DBUnitExtension;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.triplea.IntegTestExtension;

@QuarkusTest
@ExtendWith(IntegTestExtension.class)
@ExtendWith(DBUnitExtension.class)
@RequiredArgsConstructor
class LobbyLoginMessageDaoTest {
  private final LobbyLoginMessageDao lobbyLoginMessageDao;

  @Test
  void selectLobbyMessage() {
    assertThat(lobbyLoginMessageDao.get(), is(notNullValue()));
  }
}
