package org.triplea.lobby.server.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.triplea.lobby.server.ControllerIntegrationTest;
import org.triplea.lobby.server.LobbyHttpClientHelper;

@QuarkusTest
public class ModeratorsControllerIntegrationTest extends ControllerIntegrationTest {

  private static final String IS_ADMIN_PATH = "/lobby/moderator-toolbox/is-admin";
  private static final String ADD_ADMIN_PATH = "/lobby/moderator-toolbox/admin/add-super-mod";
  private static final String REMOVE_MOD_PATH = "/lobby/moderator-toolbox/admin/remove-mod";
  private static final String ADD_MODERATOR_PATH = "/lobby/moderator-toolbox/admin/add-moderator";

  LobbyHttpClientHelper playerClient;
  LobbyHttpClientHelper moderatorClient;
  LobbyHttpClientHelper adminClient;

  @BeforeEach
  void setup() {
    this.playerClient = new LobbyHttpClientHelper(localhost, PLAYER);
    this.moderatorClient = new LobbyHttpClientHelper(localhost, MODERATOR);
    this.adminClient = new LobbyHttpClientHelper(localhost, ADMIN);
  }

  @SuppressWarnings("unchecked")
  @Test
  void mustBeAuthorized() {
    assertNotAuthorized(
        List.of(PLAYER, MODERATOR),
        apiKey -> new LobbyHttpClientHelper(localhost, apiKey),
        c -> c.post(ADD_ADMIN_PATH, "admin"),
        c -> c.post(ADD_MODERATOR_PATH, "mod"),
        c -> c.post(REMOVE_MOD_PATH, "mod"));
  }

  @Test
  void isAdmin() {
    assertThat(playerClient.get(IS_ADMIN_PATH, Boolean.class), is(false));
    assertThat(moderatorClient.get(IS_ADMIN_PATH, Boolean.class), is(false));
    assertThat(adminClient.get(IS_ADMIN_PATH, Boolean.class), is(true));
  }

  @Test
  void removeMod() {
    adminClient.post(REMOVE_MOD_PATH, "mod");
  }

  @Test
  void addMod() {
    adminClient.post(ADD_MODERATOR_PATH, "mod");
  }

  @Test
  void setAdmin() {
    adminClient.post(ADD_ADMIN_PATH, "admin");
  }
}
