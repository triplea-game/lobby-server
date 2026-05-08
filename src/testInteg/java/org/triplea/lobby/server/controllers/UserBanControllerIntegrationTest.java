package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.triplea.test.common.matchers.CollectionMatchers.containsMappedItem;
import static org.triplea.test.common.matchers.CollectionMatchers.doesNotContainMappedItem;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.moderator.toolbox.banned.user.UserBanData;
import org.triplea.http.client.lobby.moderator.toolbox.banned.user.UserBanParams;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class UserBanControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String GET_USER_BANS_PATH = "/lobby/moderator-toolbox/get-user-bans";
  private static final String REMOVE_USER_BAN_PATH = "/lobby/moderator-toolbox/remove-user-ban";
  private static final String BAN_USER_PATH = "/lobby/moderator-toolbox/ban-user";

  private LobbyHttpClientHelper client;

  @BeforeEach
  void setUp() {
    client = new LobbyHttpClientHelper(localhost, MODERATOR);
  }

  private List<UserBanData> getUserBans() {
    return Arrays.asList(client.get(GET_USER_BANS_PATH, UserBanData[].class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        NOT_MODERATORS,
        apiKey -> new LobbyHttpClientHelper(localhost, apiKey),
        c -> c.get(GET_USER_BANS_PATH, UserBanData[].class),
        c ->
            c.post(
                BAN_USER_PATH,
                UserBanParams.builder()
                    .ip("ip")
                    .minutesToBan(10)
                    .systemId("system-id")
                    .username("username")
                    .build()),
        c -> c.post(REMOVE_USER_BAN_PATH, "some-username"));
  }

  @Test
  void listUserBans() {
    assertThat(getUserBans(), is(not(empty())));
  }

  @Test
  void removeUserNameBan() {
    final UserBanData firstItem = getUserBans().get(0);

    assertThat(getUserBans(), containsMappedItem(UserBanData::getBanId, firstItem.getBanId()));

    client.post(REMOVE_USER_BAN_PATH, firstItem.getBanId());

    assertThat(
        getUserBans(), doesNotContainMappedItem(UserBanData::getBanId, firstItem.getBanId()));
  }

  /**
   * Generate a mostly unique user name. <br>
   * Ensure user name is not already banned. <br>
   * Add user name to banned users. <br>
   * Verify banned users contains the new ban. <br>
   */
  @Test
  void addUserNameBan() {
    final String userNameToBan = "user-name-to-ban-" + UUID.randomUUID().toString().substring(0, 5);
    assertThat(getUserBans(), doesNotContainMappedItem(UserBanData::getUsername, userNameToBan));

    client.post(
        BAN_USER_PATH,
        UserBanParams.builder()
            .username(userNameToBan)
            .systemId("system-id")
            .minutesToBan(10)
            .ip("55.55.55.55")
            .build());

    assertThat(getUserBans(), containsMappedItem(UserBanData::getUsername, userNameToBan));
  }
}
