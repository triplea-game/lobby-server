package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.game.hosting.request.GameHostingClient;
import org.triplea.lobby.server.ControllerIntegrationTest;

@QuarkusTest
public class GameHostingControllerTest extends ControllerIntegrationTest {
  GameHostingClient client;

  @BeforeEach
  void setup() {
    client = GameHostingClient.newClient(localhost);
  }

  @Test
  void sendGameHostingRequest() {
    final var result = client.sendGameHostingRequest();
    assertThat(result, is(IsNull.notNullValue()));
  }
}
