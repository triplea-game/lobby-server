package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.triplea.test.common.matchers.CollectionMatchers.containsMappedItem;
import static org.triplea.test.common.matchers.CollectionMatchers.doesNotContainMappedItem;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.login.LobbyLoginResponse;
import org.triplea.http.client.lobby.login.LoginRequest;
import org.triplea.http.client.lobby.moderator.toolbox.banned.name.UsernameBanData;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class UsernameBanControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String GET_USERNAME_BANS_PATH = "/lobby/moderator-toolbox/get-username-bans";
  private static final String ADD_USERNAME_BAN_PATH = "/lobby/moderator-toolbox/add-username-ban";
  private static final String REMOVE_USERNAME_BAN_PATH =
      "/lobby/moderator-toolbox/remove-username-ban";
  private static final String LOGIN_PATH = "/lobby/user-login/authenticate";

  LobbyHttpClientHelper client;

  @BeforeEach
  void setup() {
    this.client = new LobbyHttpClientHelper(localhost, MODERATOR);
  }

  private List<UsernameBanData> getUsernameBans() {
    return Arrays.asList(client.get(GET_USERNAME_BANS_PATH, UsernameBanData[].class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        NOT_MODERATORS,
        apiKey -> new LobbyHttpClientHelper(localhost, apiKey),
        c -> c.get(GET_USERNAME_BANS_PATH, UsernameBanData[].class),
        c -> c.post(ADD_USERNAME_BAN_PATH, "some-username"),
        c -> c.post(REMOVE_USERNAME_BAN_PATH, "some-username"));
  }

  @Test
  void listBans() {
    final List<UsernameBanData> nameBans = getUsernameBans();
    assertThat(nameBans, is(not(empty())));
  }

  @Test
  void removeBan() {
    final List<UsernameBanData> nameBans = getUsernameBans();
    final UsernameBanData firstItem = nameBans.get(0);

    client.post(REMOVE_USERNAME_BAN_PATH, firstItem.getBannedName());

    assertThat(getUsernameBans(), is(not(hasItem(firstItem))));
  }

  @Test
  void addBan() {
    assertThat(
        "Make sure bans does not contain the item we will add",
        getUsernameBans(),
        doesNotContainMappedItem(
            UsernameBanData::getBannedName, "username-that-is-now-banned".toUpperCase()));

    client.post(ADD_USERNAME_BAN_PATH, "username-that-is-now-banned");

    assertThat(
        "Bans should now contain the newly added item",
        getUsernameBans(),
        containsMappedItem(
            UsernameBanData::getBannedName, "username-that-is-now-banned".toUpperCase()));
  }

  /**
   * Do a login to verify we can login. Ban the name we used for login, then repeat the login and
   * verify the login is not successful.
   */
  @Test
  void usernameBanDisallowsLogin() {
    final LobbyHttpClientHelper loginClient = new LobbyHttpClientHelper(localhost);

    LobbyLoginResponse loginResponse =
        loginClient.post(
            LOGIN_PATH,
            LoginRequest.builder().name("random-user").password(null).build(),
            LobbyLoginResponse.class);
    assertThat("Verify our anonymous login worked", loginResponse.isSuccess(), is(true));

    client.post(ADD_USERNAME_BAN_PATH, "random-user");

    loginResponse =
        loginClient.post(
            LOGIN_PATH,
            LoginRequest.builder().name("random-user").password(null).build(),
            LobbyLoginResponse.class);
    assertThat("Verify our anonymous login worked", loginResponse.isSuccess(), is(false));
  }
}
