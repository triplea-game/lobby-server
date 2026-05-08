package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.login.CreateAccountRequest;
import org.triplea.http.client.lobby.login.CreateAccountResponse;
import org.triplea.http.client.lobby.login.LobbyLoginResponse;
import org.triplea.http.client.lobby.login.LoginRequest;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class CreateAccountControllerIntegrationTest extends ControllerIntegrationTest {
  private static final String LOGIN_PATH = "/lobby/user-login/authenticate";
  private static final String CREATE_PATH = "/lobby/user-login/create-account";

  private static final String USERNAME = "user-name";
  private static final String EMAIL = "email@email.com";
  private static final String PASSWORD = hashPasswordWithSalt("pass");

  private static final String USERNAME_1 = "user-name_1";
  private static final String EMAIL_1 = "email1@email.com";
  private static final String PASSWORD_1 = hashPasswordWithSalt("pass_1");

  LobbyHttpClientHelper client;

  @BeforeEach
  void setup() {
    this.client = new LobbyHttpClientHelper(localhost);
  }

  private LobbyLoginResponse login(final String username, final String password) {
    return client.post(
        LOGIN_PATH,
        LoginRequest.builder().name(username).password(password).build(),
        LobbyLoginResponse.class);
  }

  private CreateAccountResponse createAccount(
      final String username, final String email, final String password) {
    return client.post(
        CREATE_PATH,
        CreateAccountRequest.builder().username(username).email(email).password(password).build(),
        CreateAccountResponse.class);
  }

  @Test
  void badRequests() {
    assertBadRequest(() -> login(null, null));
    assertBadRequest(() -> createAccount(null, null, null));
    assertBadRequest(() -> createAccount("user", "email@email.com", null));
    assertBadRequest(() -> createAccount("user", null, "password"));
    assertBadRequest(() -> createAccount(null, "email@email.com", "password"));
  }

  @Test
  void createAccountAndDoLogin() {
    final CreateAccountResponse result = createAccount(USERNAME, EMAIL, PASSWORD);
    assertThat(result, is(CreateAccountResponse.SUCCESS_RESPONSE));

    final var loginResponse = login(USERNAME, PASSWORD);
    assertThat(loginResponse.isSuccess(), is(true));
  }

  @Test
  void duplicateAccountCreateFails() {
    createAccount(USERNAME_1, EMAIL_1, PASSWORD_1);

    final CreateAccountResponse result = createAccount(USERNAME_1, EMAIL_1, PASSWORD_1);

    assertThat(result, is(not(CreateAccountResponse.SUCCESS_RESPONSE)));
  }

  private static String hashPasswordWithSalt(final String password) {
    if (password.isBlank()) {
      return password;
    }
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-512")
                  .digest(("TripleA" + password).getBytes(StandardCharsets.UTF_8)))
          .toLowerCase(Locale.ROOT);
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-512 is not supported!", e);
    }
  }
}
