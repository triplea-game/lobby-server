package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.lobby.moderator.toolbox.management.ToolboxModeratorManagementClient;
import org.triplea.lobby.server.ControllerIntegrationTest;

@QuarkusTest
public class ModeratorsControllerIntegrationTest extends ControllerIntegrationTest {
  ToolboxModeratorManagementClient playerClient;
  ToolboxModeratorManagementClient moderatorClient;
  ToolboxModeratorManagementClient adminClient;

  @BeforeEach
  void setup() {
    this.playerClient = ToolboxModeratorManagementClient.newClient(localhost, PLAYER);
    this.moderatorClient = ToolboxModeratorManagementClient.newClient(localhost, MODERATOR);
    this.adminClient = ToolboxModeratorManagementClient.newClient(localhost, ADMIN);
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        List.of(PLAYER, MODERATOR),
        apiKey -> ToolboxModeratorManagementClient.newClient(localhost, apiKey),
        client -> client.addAdmin("admin"),
        client -> client.addModerator("mod"),
        client -> client.removeMod("mod"));
  }

  @Test
  void isAdmin() {
    assertThat(playerClient.isCurrentUserAdmin(), is(false));
    assertThat(moderatorClient.isCurrentUserAdmin(), is(false));
    assertThat(adminClient.isCurrentUserAdmin(), is(true));
  }

  @Test
  void removeMod() {
    adminClient.removeMod("mod");
  }

  @Test
  void addMod() {
    adminClient.addModerator("mod");
  }

  @Test
  void setAdmin() {
    adminClient.addAdmin("admin");
  }
}
