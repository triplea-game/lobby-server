package org.triplea.lobby.server.controllers;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.forgot.password.ForgotPasswordClient;
import org.triplea.http.client.forgot.password.ForgotPasswordRequest;
import org.triplea.lobby.server.ControllerIntegrationTest;

@QuarkusTest
public class ForgotPasswordControllerIntegrationTest extends ControllerIntegrationTest {
  ForgotPasswordClient client;

  @BeforeEach
  void setup() {
    this.client = ForgotPasswordClient.newClient(localhost);
  }

  @Test
  void badArgs() {
    assertBadRequest(
        () -> client.sendForgotPasswordRequest(ForgotPasswordRequest.builder().build()));
  }

  @Test
  void forgotPassword() {
    client.sendForgotPasswordRequest(
        ForgotPasswordRequest.builder().username("user").email("email").build());
  }
}
