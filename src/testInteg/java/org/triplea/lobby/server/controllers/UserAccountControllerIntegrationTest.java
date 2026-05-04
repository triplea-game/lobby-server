package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.user.account.UserAccountClient;
import org.triplea.lobby.server.ControllerIntegrationTest;

@QuarkusTest
public class UserAccountControllerIntegrationTest extends ControllerIntegrationTest {
  UserAccountClient client;

  @BeforeEach
  void setup() {
    client = UserAccountClient.newClient(localhost, PLAYER);
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        List.of(ANONYMOUS),
        apiKey -> UserAccountClient.newClient(localhost, apiKey),
        UserAccountClient::fetchEmail,
        client -> client.changeEmail("new-email"),
        client -> client.changePassword("new-password"));
  }

  @Test
  void changePassword() {
    client.changePassword("password");
  }

  @Test
  void fetchEmail() {
    assertThat(client.fetchEmail(), notNullValue());
  }

  @Test
  void changeEmail() {
    assertThat(client.fetchEmail(), is(not("email@email-test.com")));

    client.changeEmail("email@email-test.com");

    assertThat(client.fetchEmail().getUserEmail(), is("email@email-test.com"));
  }
}
