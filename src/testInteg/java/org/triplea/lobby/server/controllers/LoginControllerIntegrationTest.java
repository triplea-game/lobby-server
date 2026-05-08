package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import io.quarkus.test.junit.QuarkusTest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.login.LobbyLoginResponse;
import org.triplea.http.client.lobby.login.LoginRequest;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class LoginControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String LOGIN_PATH = "/lobby/user-login/authenticate";

  private static final String USERNAME = "player";
  private static final String PASSWORD = hashPasswordWithSalt("password");
  private static final String TEMP_PASSWORD = hashPasswordWithSalt("temp-password");
  private static final String INVALID_PASSWORD = "invalid";

  LobbyHttpClientHelper client;

  @BeforeEach
  void setup() {
    client = new LobbyHttpClientHelper(localhost);
  }

  private LobbyLoginResponse login(final String username, final String password) {
    return client.post(
        LOGIN_PATH,
        LoginRequest.builder().name(username).password(password).build(),
        LobbyLoginResponse.class);
  }

  @Test
  void invalidLogin() {
    final LobbyLoginResponse response = login(USERNAME, INVALID_PASSWORD);

    assertThat(response.getFailReason(), notNullValue());
    assertThat(response.getApiKey(), nullValue());
    assertThat(response.isPasswordChangeRequired(), is(false));
  }

  @Test
  void validLogin() {
    final LobbyLoginResponse response = login(USERNAME, PASSWORD);

    assertThat(response.getFailReason(), nullValue());
    assertThat(response.getApiKey(), notNullValue());
    assertThat(response.isPasswordChangeRequired(), is(false));
  }

  @Test
  void tempPasswordLogin() {
    final LobbyLoginResponse response = login(USERNAME, TEMP_PASSWORD);

    assertThat(response.getFailReason(), nullValue());
    assertThat(response.getApiKey(), notNullValue());
    assertThat(response.isPasswordChangeRequired(), is(true));
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
