package org.triplea.lobby.server.controllers;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.forgot.password.ForgotPasswordRequest;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class ForgotPasswordControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String FORGOT_PASSWORD_PATH = "/lobby/forgot-password";

  LobbyHttpClientHelper client;

  @BeforeEach
  void setup() {
    this.client = new LobbyHttpClientHelper(localhost);
  }

  @Test
  void badArgs() {
    assertBadRequest(
        () -> client.post(FORGOT_PASSWORD_PATH, ForgotPasswordRequest.builder().build()));
  }

  @Test
  void forgotPassword() {
    client.post(
        FORGOT_PASSWORD_PATH,
        ForgotPasswordRequest.builder().username("user").email("email").build());
  }
}
