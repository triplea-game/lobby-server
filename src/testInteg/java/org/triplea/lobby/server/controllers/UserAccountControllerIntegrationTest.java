package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.user.account.FetchEmailResponse;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class UserAccountControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String CHANGE_PASSWORD_PATH = "/lobby/user-account/change-password";
  private static final String FETCH_EMAIL_PATH = "/lobby/user-account/fetch-email";
  private static final String CHANGE_EMAIL_PATH = "/lobby/user-account/change-email";

  LobbyHttpClientHelper client;

  @BeforeEach
  void setup() {
    client = new LobbyHttpClientHelper(localhost, PLAYER);
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        List.of(ANONYMOUS),
        apiKey -> new LobbyHttpClientHelper(localhost, apiKey),
        c -> c.get(FETCH_EMAIL_PATH, FetchEmailResponse.class),
        c -> c.post(CHANGE_EMAIL_PATH, "new-email"),
        c -> c.post(CHANGE_PASSWORD_PATH, "new-password"));
  }

  @Test
  void changePassword() {
    client.post(CHANGE_PASSWORD_PATH, "password");
  }

  @Test
  void fetchEmail() {
    assertThat(client.get(FETCH_EMAIL_PATH, FetchEmailResponse.class), notNullValue());
  }

  @Test
  void changeEmail() {
    assertThat(
        client.get(FETCH_EMAIL_PATH, FetchEmailResponse.class), is(not("email@email-test.com")));

    client.post(CHANGE_EMAIL_PATH, "email@email-test.com");

    assertThat(
        client.get(FETCH_EMAIL_PATH, FetchEmailResponse.class).getUserEmail(),
        is("email@email-test.com"));
  }
}
